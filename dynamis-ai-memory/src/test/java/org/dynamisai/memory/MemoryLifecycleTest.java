package org.dynamisai.memory;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MemoryLifecycleTest {

    private DefaultMemoryLifecycleManager manager;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        manager = new DefaultMemoryLifecycleManager(
            MemoryBudget.tier1(), new InHeapVectorMemoryStore());
        npc = EntityId.of(1L);
    }

    private MemoryRecord raw(float importance) {
        return MemoryRecord.rawEvent(npc, "test event", "payload", importance);
    }

    @Test
    void addRawEventIsStoredAtRawStage() {
        manager.addRawEvent(raw(0.5f));
        List<MemoryRecord> records =
            manager.getMemoriesAtStage(npc, MemoryLifecycleStage.RAW_EVENT);
        assertEquals(1, records.size());
        assertEquals(MemoryLifecycleStage.RAW_EVENT, records.get(0).stage());
    }

    @Test
    void addRawEventRejectsNonRawRecord() {
        MemoryRecord nonRaw = raw(0.5f).withStage(MemoryLifecycleStage.SHORT_TERM);
        assertThrows(IllegalArgumentException.class, () -> manager.addRawEvent(nonRaw));
    }

    @Test
    void consolidatePromotesHighImportanceRawToShortTerm() {
        manager.addRawEvent(raw(0.8f));
        manager.consolidate(npc);
        assertTrue(manager.getMemoriesAtStage(npc,
            MemoryLifecycleStage.RAW_EVENT).isEmpty());
        assertEquals(1, manager.getMemoriesAtStage(npc,
            MemoryLifecycleStage.SHORT_TERM).size());
    }

    @Test
    void consolidateDoesNotPromoteLowImportanceRaw() {
        manager.addRawEvent(raw(0.1f));
        manager.consolidate(npc);
        assertEquals(1, manager.getMemoriesAtStage(npc,
            MemoryLifecycleStage.RAW_EVENT).size());
    }

    @Test
    void consolidatePromotesHighImportanceShortTermToConsolidated() {
        manager.addRawEvent(raw(0.8f));
        manager.consolidate(npc);
        manager.consolidate(npc);
        assertEquals(1, manager.getMemoriesAtStage(npc,
            MemoryLifecycleStage.CONSOLIDATED).size());
    }

    @Test
    void memoryBudgetPrunesExcessRawEvents() {
        MemoryBudget tinyBudget = new MemoryBudget(3, 5, 5, 10);
        DefaultMemoryLifecycleManager tightManager =
            new DefaultMemoryLifecycleManager(tinyBudget, new InHeapVectorMemoryStore());
        EntityId owner = EntityId.of(2L);

        for (int i = 0; i < 5; i++) {
            tightManager.addRawEvent(MemoryRecord.rawEvent(
                owner, "event " + i, null, 0.5f));
        }
        List<MemoryRecord> raw =
            tightManager.getMemoriesAtStage(owner, MemoryLifecycleStage.RAW_EVENT);
        assertTrue(raw.size() <= 3,
            "Budget should limit raw events to 3 but was " + raw.size());
    }

    @Test
    void purgeAllRemovesAllRecords() {
        manager.addRawEvent(raw(0.9f));
        manager.consolidate(npc);
        manager.purgeAll(npc);
        assertTrue(manager.getMemories(npc).isEmpty());
    }

    @Test
    void getByIdFindsActiveRecord() {
        MemoryRecord r = raw(0.5f);
        manager.addRawEvent(r);
        assertTrue(manager.getById(r.id()).isPresent());
    }

    @Test
    void getByIdReturnsEmptyForUnknownId() {
        assertTrue(manager.getById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void statsReflectCurrentState() {
        manager.addRawEvent(raw(0.9f));
        manager.addRawEvent(raw(0.2f));
        MemoryStats stats = manager.getStats(npc);
        assertEquals(2, stats.rawEventCount());
        assertEquals(2, stats.totalCount());
    }

    @Test
    void statsAfterConsolidation() {
        manager.addRawEvent(raw(0.9f));
        manager.addRawEvent(raw(0.1f));
        manager.consolidate(npc);
        MemoryStats stats = manager.getStats(npc);
        assertEquals(1, stats.rawEventCount());
        assertEquals(1, stats.shortTermCount());
    }

    @Test
    void memoryRecordImmutableOnStageTransition() {
        MemoryRecord original = raw(0.7f);
        MemoryRecord promoted = original.withStage(MemoryLifecycleStage.SHORT_TERM);
        assertEquals(MemoryLifecycleStage.RAW_EVENT, original.stage());
        assertEquals(MemoryLifecycleStage.SHORT_TERM, promoted.stage());
        assertEquals(original.id(), promoted.id());
    }

    @Test
    void memoryRecordImportanceValidation() {
        assertThrows(IllegalArgumentException.class, () ->
            MemoryRecord.rawEvent(npc, "bad", null, 1.5f));
        assertThrows(IllegalArgumentException.class, () ->
            MemoryRecord.rawEvent(npc, "bad", null, -0.1f));
    }

    @Test
    void vectorMemoryStoreStoreAndRetrieve() {
        InHeapVectorMemoryStore store = new InHeapVectorMemoryStore();
        MemoryRecord r = raw(0.8f).withStage(MemoryLifecycleStage.ARCHIVED);
        store.store(r);
        assertTrue(store.getById(r.id()).isPresent());
        assertEquals(1, store.countForOwner(npc));
    }

    @Test
    void vectorMemoryStoreFindSimilarMatchesSummary() {
        InHeapVectorMemoryStore store = new InHeapVectorMemoryStore();
        MemoryRecord r = new MemoryRecord(UUID.randomUUID(), npc,
            MemoryLifecycleStage.ARCHIVED, 0.8f,
            Instant.now(), Instant.now(),
            "player burned the tavern", "payload");
        store.store(r);
        List<MemoryRecord> results = store.findSimilar(npc, "tavern", 5);
        assertEquals(1, results.size());
    }

    @Test
    void vectorMemoryStoreRemove() {
        InHeapVectorMemoryStore store = new InHeapVectorMemoryStore();
        MemoryRecord r = raw(0.5f).withStage(MemoryLifecycleStage.ARCHIVED);
        store.store(r);
        store.remove(r.id());
        assertTrue(store.getById(r.id()).isEmpty());
    }

    @Test
    void memoryBudgetTierFactories() {
        assertDoesNotThrow(MemoryBudget::tier0);
        assertDoesNotThrow(MemoryBudget::tier1);
        assertDoesNotThrow(MemoryBudget::background);
    }

    @Test
    void memoryBudgetRejectsZeroCapacity() {
        assertThrows(IllegalArgumentException.class, () ->
            new MemoryBudget(0, 5, 5, 10));
    }
}
