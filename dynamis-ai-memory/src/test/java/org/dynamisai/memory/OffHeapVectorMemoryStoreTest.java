package org.dynamisai.memory;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OffHeapVectorMemoryStoreTest {

    private static final int DIM = 16;
    private static final EntityId OWNER = EntityId.of(1L);

    private OffHeapVectorMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new OffHeapVectorMemoryStore(DIM, 8);
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    @Test
    void storeStartsEmpty() {
        assertEquals(0, store.size());
    }

    @Test
    void rejectsDimZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new OffHeapVectorMemoryStore(0, 8));
    }

    @Test
    void rejectsCapacityZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new OffHeapVectorMemoryStore(DIM, 0));
    }

    @Test
    void storeSingleRecordIncreasesSize() {
        store.store(makeRecord("hello world"), makeVec(DIM, 1f));
        assertEquals(1, store.size());
    }

    @Test
    void storeMultipleRecordsAccumulates() {
        for (int i = 0; i < 5; i++) {
            store.store(makeRecord("event " + i), makeVec(DIM, i + 1f));
        }
        assertEquals(5, store.size());
    }

    @Test
    void storeSameRecordIdUpdatesWithoutGrowingSize() {
        MemoryRecord rec = makeRecord("same-id");
        store.store(rec, makeVec(DIM, 1f));
        store.store(rec, makeVec(DIM, 2f));
        assertEquals(1, store.size());
    }

    @Test
    void storeRejectsMismatchedDim() {
        assertThrows(IllegalArgumentException.class, () ->
            store.store(makeRecord("bad"), makeVec(DIM + 1, 1f)));
    }

    @Test
    void removeDecreasesSize() {
        MemoryRecord rec = makeRecord("removable");
        store.store(rec, makeVec(DIM, 1f));
        assertEquals(1, store.size());
        store.remove(rec.id());
        assertEquals(0, store.size());
    }

    @Test
    void removeIsIdempotent() {
        MemoryRecord rec = makeRecord("x");
        store.store(rec, makeVec(DIM, 1f));
        assertDoesNotThrow(() -> {
            store.remove(rec.id());
            store.remove(rec.id());
        });
    }

    @Test
    void removeNonExistentIdDoesNotThrow() {
        assertDoesNotThrow(() -> store.remove(UUID.randomUUID()));
    }

    @Test
    void findSimilarReturnsEmptyWhenEmpty() throws Exception {
        List<SimilarityResult> results = store.findSimilar(
            makeVec(DIM, 1f), OWNER, 5).get(3, TimeUnit.SECONDS);
        assertTrue(results.isEmpty());
    }

    @Test
    void findSimilarReturnsIdenticalVectorFirst() throws Exception {
        float[] pattern = new float[DIM];
        pattern[0] = 1.0f;
        EmbeddingVector target = new EmbeddingVector(pattern);
        MemoryRecord rec = makeRecord("target");
        store.store(rec, target);
        store.store(makeRecord("noise"), makeVec(DIM, 0.1f));

        List<SimilarityResult> results = store.findSimilar(
            target, OWNER, 2).get(3, TimeUnit.SECONDS);

        assertFalse(results.isEmpty());
        assertEquals(rec.id(), results.get(0).record().id(),
            "Identical vector should rank first");
    }

    @Test
    void findSimilarRespectsTopK() throws Exception {
        for (int i = 0; i < 10; i++) {
            store.store(makeRecord("rec" + i), makeVec(DIM, i + 1f));
        }
        List<SimilarityResult> results = store.findSimilar(
            makeVec(DIM, 1f), OWNER, 3).get(3, TimeUnit.SECONDS);
        assertTrue(results.size() <= 3);
    }

    @Test
    void findSimilarSortedDescending() throws Exception {
        for (int i = 0; i < 5; i++) {
            store.store(makeRecord("r" + i), makeVec(DIM, i + 0.5f));
        }
        List<SimilarityResult> results = store.findSimilar(
            makeVec(DIM, 1f), OWNER, 5).get(3, TimeUnit.SECONDS);
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).similarity() >= results.get(i).similarity(),
                "Results must be sorted descending");
        }
    }

    @Test
    void findSimilarFiltersOwner() throws Exception {
        EntityId other = EntityId.of(99L);
        MemoryRecord ownRec = makeRecord("mine", OWNER);
        MemoryRecord otherRec = makeRecord("theirs", other);
        store.store(ownRec, makeVec(DIM, 1f));
        store.store(otherRec, makeVec(DIM, 1f));

        List<SimilarityResult> results = store.findSimilar(
            makeVec(DIM, 1f), OWNER, 10).get(3, TimeUnit.SECONDS);

        assertTrue(results.stream().allMatch(r -> r.record().owner().equals(OWNER)),
            "findSimilar must only return records owned by the querying entity");
    }

    @Test
    void findSimilarRejectsWrongDim() {
        assertThrows(IllegalArgumentException.class, () ->
            store.findSimilar(makeVec(DIM + 1, 1f), OWNER, 5));
    }

    @Test
    void storeGrowsBeyondInitialCapacity() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 20; i++) {
                store.store(makeRecord("r" + i), makeVec(DIM, i + 1f));
            }
        });
        assertEquals(20, store.size());
    }

    @Test
    void queryAfterGrowthStillWorks() throws Exception {
        for (int i = 0; i < 20; i++) {
            store.store(makeRecord("r" + i), makeVec(DIM, i + 1f));
        }
        List<SimilarityResult> results = store.findSimilar(
            makeVec(DIM, 1f), OWNER, 5).get(3, TimeUnit.SECONDS);
        assertFalse(results.isEmpty());
    }

    @Test
    void closeIsIdempotent() {
        assertDoesNotThrow(() -> {
            store.close();
            store.close();
        });
    }

    @Test
    void storeAfterCloseThrows() {
        store.close();
        assertThrows(IllegalStateException.class, () ->
            store.store(makeRecord("late"), makeVec(DIM, 1f)));
    }

    @Test
    void findSimilarAfterCloseThrows() {
        store.close();
        assertThrows(IllegalStateException.class, () ->
            store.findSimilar(makeVec(DIM, 1f), OWNER, 5));
    }

    @Test
    void embeddingVectorRejectsEmpty() {
        assertThrows(IllegalArgumentException.class,
            () -> new EmbeddingVector(new float[0]));
    }

    @Test
    void cosineSimilarityOfIdenticalVectorsIsOne() {
        float[] v = {1f, 0f, 0f, 0f};
        EmbeddingVector a = new EmbeddingVector(v);
        assertEquals(1f, a.cosineSimilarity(a), 0.001f);
    }

    @Test
    void cosineSimilarityOfOrthogonalVectorsIsZero() {
        EmbeddingVector a = new EmbeddingVector(new float[]{1f, 0f, 0f, 0f});
        EmbeddingVector b = new EmbeddingVector(new float[]{0f, 1f, 0f, 0f});
        assertEquals(0f, a.cosineSimilarity(b), 0.001f);
    }

    @Test
    void normalizeProducesUnitVector() {
        EmbeddingVector v = new EmbeddingVector(new float[]{3f, 4f, 0f, 0f});
        EmbeddingVector n = v.normalize();
        float mag = 0;
        for (int i = 0; i < n.dim(); i++) {
            mag += n.get(i) * n.get(i);
        }
        assertEquals(1f, mag, 0.001f);
    }

    @Test
    void cosineSimilarityRejectsDimMismatch() {
        EmbeddingVector a = new EmbeddingVector(new float[]{1f, 0f});
        EmbeddingVector b = new EmbeddingVector(new float[]{1f, 0f, 0f});
        assertThrows(IllegalArgumentException.class, () -> a.cosineSimilarity(b));
    }

    @Test
    void inHeapAdapterStoresAndSizes() {
        InHeapVectorMemoryStore legacy = new InHeapVectorMemoryStore();
        try {
            legacy.store(makeRecord("test"));
            assertEquals(1, legacy.size());
        } finally {
            legacy.close();
        }
    }

    @Test
    void keywordEmbeddingIsDeterministic() {
        EmbeddingVector a = InHeapVectorMemoryStore.keywordEmbedding("hello world", 32);
        EmbeddingVector b = InHeapVectorMemoryStore.keywordEmbedding("hello world", 32);
        assertTrue(a.approximatelyEquals(b, 1e-6f));
    }

    @Test
    void keywordEmbeddingDifferentTextsDiffer() {
        EmbeddingVector a = InHeapVectorMemoryStore.keywordEmbedding("hello", 32);
        EmbeddingVector b = InHeapVectorMemoryStore.keywordEmbedding("world", 32);
        assertFalse(a.approximatelyEquals(b, 1e-6f));
    }

    private MemoryRecord makeRecord(String summary) {
        return makeRecord(summary, OWNER);
    }

    private MemoryRecord makeRecord(String summary, EntityId owner) {
        return MemoryRecord.create(owner, summary, "payload:" + summary, 0.5f);
    }

    private EmbeddingVector makeVec(int dim, float seed) {
        float[] v = new float[dim];
        for (int i = 0; i < dim; i++) {
            v[i] = (float) Math.sin(seed + i);
        }
        return new EmbeddingVector(v);
    }
}
