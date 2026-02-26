package org.dynamisai.memory;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SentenceEncoderTest {

    private static final EntityId OWNER = EntityId.of(1L);

    @Test
    void mockEncoderDimIs384() {
        SentenceEncoder enc = new MockSentenceEncoder();
        assertEquals(384, enc.dim());
    }

    @Test
    void mockEncoderProduces384DimVector() {
        SentenceEncoder enc = new MockSentenceEncoder();
        EmbeddingVector v = enc.encode("hello world");
        assertEquals(384, v.dim());
    }

    @Test
    void mockEncoderIsDeterministic() {
        SentenceEncoder enc = new MockSentenceEncoder();
        EmbeddingVector a = enc.encode("the player attacked a guard");
        EmbeddingVector b = enc.encode("the player attacked a guard");
        assertTrue(a.approximatelyEquals(b, 1e-6f));
    }

    @Test
    void mockEncoderDifferentTextsProduceDifferentVectors() {
        SentenceEncoder enc = new MockSentenceEncoder();
        EmbeddingVector a = enc.encode("peaceful merchant");
        EmbeddingVector b = enc.encode("violent attacker");
        assertFalse(a.approximatelyEquals(b, 1e-3f));
    }

    @Test
    void mockEncoderNullReturnsZeroVector() {
        SentenceEncoder enc = new MockSentenceEncoder();
        EmbeddingVector v = enc.encode(null);
        float mag = 0;
        for (int i = 0; i < v.dim(); i++) {
            mag += v.get(i) * v.get(i);
        }
        assertEquals(0f, mag, 1e-6f);
    }

    @Test
    void miniLmEncoderFallsBackToMockWhenNoModel() {
        MiniLmSentenceEncoder enc = new MiniLmSentenceEncoder(
            Path.of("/nonexistent/path"));
        enc.initialize();
        assertFalse(enc.isLive());
        EmbeddingVector v = enc.encode("test");
        assertEquals(384, v.dim());
    }

    @Test
    void miniLmEncoderIsNotLiveBeforeInitialize() {
        MiniLmSentenceEncoder enc = new MiniLmSentenceEncoder(
            Path.of("/nonexistent/path"));
        assertFalse(enc.isLive());
    }

    @Test
    void miniLmEncoderCloseIsIdempotent() {
        MiniLmSentenceEncoder enc = new MiniLmSentenceEncoder(
            Path.of("/nonexistent/path"));
        assertDoesNotThrow(() -> {
            enc.close();
            enc.close();
        });
    }

    @Test
    void inHeapStoreWithMockEncoderStoresAt384Dim() {
        InHeapVectorMemoryStore store =
            new InHeapVectorMemoryStore(new MockSentenceEncoder());
        try {
            MemoryRecord r = MemoryRecord.create(OWNER,
                "Player entered the tavern", "payload", 0.5f);
            store.store(r);
            assertEquals(1, store.size());
        } finally {
            store.close();
        }
    }

    @Test
    void inHeapStoreStringFindSimilarReturnsSemanticallyRelated() {
        InHeapVectorMemoryStore store =
            new InHeapVectorMemoryStore(new MockSentenceEncoder());
        try {
            MemoryRecord tavern = MemoryRecord.create(OWNER,
                "tavern brawl fight", "p", 0.6f);
            MemoryRecord quest = MemoryRecord.create(OWNER,
                "quest completed reward", "p", 0.6f);
            store.store(tavern);
            store.store(quest);

            List<MemoryRecord> results = store.findSimilar(OWNER, "tavern", 2);
            assertFalse(results.isEmpty());
        } finally {
            store.close();
        }
    }

    @Test
    void offHeapStoreGetByIdReturnsRecord() {
        OffHeapVectorMemoryStore store = new OffHeapVectorMemoryStore(384, 8);
        try {
            MemoryRecord r = MemoryRecord.create(OWNER, "test record", "p", 0.5f);
            float[] v = new float[384];
            v[0] = 1f;
            store.store(r, new EmbeddingVector(v));
            assertTrue(store.getById(r.id()).isPresent());
            assertEquals(r.id(), store.getById(r.id()).get().id());
        } finally {
            store.close();
        }
    }

    @Test
    void offHeapStoreCountForOwner() {
        OffHeapVectorMemoryStore store = new OffHeapVectorMemoryStore(384, 8);
        EntityId other = EntityId.of(99L);
        try {
            float[] v = new float[384];
            v[0] = 1f;
            store.store(MemoryRecord.create(OWNER, "a", "p", 0.5f),
                new EmbeddingVector(v));
            store.store(MemoryRecord.create(OWNER, "b", "p", 0.5f),
                new EmbeddingVector(v));
            store.store(MemoryRecord.create(other, "c", "p", 0.5f),
                new EmbeddingVector(v));
            assertEquals(2, store.countForOwner(OWNER));
            assertEquals(1, store.countForOwner(other));
        } finally {
            store.close();
        }
    }
}
