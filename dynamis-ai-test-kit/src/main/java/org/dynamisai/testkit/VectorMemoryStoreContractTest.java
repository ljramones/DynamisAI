package org.dynamisai.testkit;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.memory.EmbeddingVector;
import org.dynamisai.memory.MemoryRecord;
import org.dynamisai.memory.VectorMemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public abstract class VectorMemoryStoreContractTest {

    protected abstract VectorMemoryStore createSubject();

    protected abstract int dim();

    private VectorMemoryStore store;
    private static final EntityId OWNER = EntityId.of(1L);

    @BeforeEach
    void setUp() {
        store = createSubject();
    }

    @AfterEach
    void tearDown() {
        store.close();
    }

    @Test
    void storeAndRetrieveById() throws Exception {
        MemoryRecord r = MemoryRecord.create(OWNER, "test", "payload", 0.5f);
        store.store(r, unitVector());
        var results = store.findSimilar(unitVector(), OWNER, 5)
            .get(3, TimeUnit.SECONDS);
        assertFalse(results.isEmpty());
        assertEquals(r.id(), results.get(0).record().id());
    }

    @Test
    void removedRecordNotReturned() throws Exception {
        MemoryRecord r = MemoryRecord.create(OWNER, "removable", "p", 0.5f);
        store.store(r, unitVector());
        store.remove(r.id());
        var results = store.findSimilar(unitVector(), OWNER, 5)
            .get(3, TimeUnit.SECONDS);
        assertTrue(results.stream().noneMatch(s -> s.record().id().equals(r.id())));
    }

    @Test
    void ownerFilterExcludesOtherOwners() throws Exception {
        EntityId other = EntityId.of(2L);
        MemoryRecord mine = MemoryRecord.create(OWNER, "mine", "p", 0.5f);
        MemoryRecord yours = MemoryRecord.create(other, "yours", "p", 0.5f);
        store.store(mine, unitVector());
        store.store(yours, unitVector());
        var results = store.findSimilar(unitVector(), OWNER, 10)
            .get(3, TimeUnit.SECONDS);
        assertTrue(results.stream().allMatch(s -> s.record().owner().equals(OWNER)));
    }

    @Test
    void similarityResultsDescending() throws Exception {
        float[] v1 = new float[dim()];
        v1[0] = 1f;
        float[] v2 = new float[dim()];
        v2[1] = 1f;
        store.store(MemoryRecord.create(OWNER, "a", "p", 0.5f), new EmbeddingVector(v1));
        store.store(MemoryRecord.create(OWNER, "b", "p", 0.5f), new EmbeddingVector(v2));
        var results = store.findSimilar(new EmbeddingVector(v1), OWNER, 5)
            .get(3, TimeUnit.SECONDS);
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).similarity() >= results.get(i).similarity());
        }
    }

    @Test
    void closeIsIdempotent() {
        assertDoesNotThrow(() -> {
            store.close();
            store.close();
        });
    }

    private EmbeddingVector unitVector() {
        float[] v = new float[dim()];
        v[0] = 1f;
        return new EmbeddingVector(v);
    }
}
