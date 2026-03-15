package org.dynamisengine.ai.extensions;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.memory.EmbeddingVector;
import org.dynamisengine.ai.memory.MemoryRecord;
import org.dynamisengine.ai.memory.VectorMemoryStore;
import org.dynamisengine.ai.testkit.VectorMemoryStoreContractTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExampleVectorMemoryStoreTest extends VectorMemoryStoreContractTest {

    @Override
    protected VectorMemoryStore createSubject() {
        return new ExampleVectorMemoryStore();
    }

    @Override
    protected int dim() {
        return 384;
    }

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleVectorMemoryStore store = new ExampleVectorMemoryStore();
        EntityId owner = EntityId.of(7L);
        MemoryRecord record = MemoryRecord.create(owner, "summary", "payload", 0.5f);
        EmbeddingVector vec = new ExampleSentenceEncoder().encode("summary");

        store.store(record, vec);
        store.store(MemoryRecord.create(owner, "summary2", "payload", 0.5f));
        assertEquals(2, store.size());
        assertFalse(store.findSimilar(owner, "summary", 5).isEmpty());
        Optional<MemoryRecord> byId = store.getById(record.id());
        assertTrue(byId.isPresent());
        assertFalse(store.getAllForOwner(owner).isEmpty());
        assertTrue(store.countForOwner(owner) >= 1);
        store.remove(record.id());
        store.close();
    }
}
