package org.dynamisai.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class DefaultWorldStateStoreTest {

    private DefaultWorldStateStore store;

    @BeforeEach
    void setUp() {
        store = new DefaultWorldStateStore();
    }

    @Test
    void initialSnapshotIsAtTickZero() {
        assertEquals(0L, store.getCurrentSnapshot().tick());
    }

    @Test
    void commitTickIncrementsTick() {
        store.commitTick();
        assertEquals(1L, store.getCurrentSnapshot().tick());
        store.commitTick();
        assertEquals(2L, store.getCurrentSnapshot().tick());
    }

    @Test
    void deterministicSeedMatchesHashOfTick() {
        store.commitTick();
        WorldSnapshot snap = store.getCurrentSnapshot();
        assertEquals(Long.hashCode(1L), snap.deterministicSeed());
    }

    @Test
    void seedForIsDeterministic() {
        store.commitTick();
        WorldSnapshot snap = store.getCurrentSnapshot();
        EntityId id = EntityId.of(42L);
        long seed1 = snap.seedFor(id);
        long seed2 = snap.seedFor(id);
        assertEquals(seed1, seed2);
    }

    @Test
    void seedForDiffersByEntityId() {
        store.commitTick();
        WorldSnapshot snap = store.getCurrentSnapshot();
        long seedA = snap.seedFor(EntityId.of(1L));
        long seedB = snap.seedFor(EntityId.of(2L));
        assertNotEquals(seedA, seedB);
    }

    @Test
    void structuralSharingDoesNotMutatePreviousSnapshot() {
        EntityId id = EntityId.of(1L);
        Location loc1 = new Location(0, 0, 0);
        Location loc2 = new Location(10, 0, 0);
        EntityState state1 = new EntityState(id, loc1, Map.of());
        EntityState state2 = new EntityState(id, loc2, Map.of());

        store.enqueueChange(new WorldChange.EntityStateChange(id, state1));
        store.commitTick();
        WorldSnapshot snap1 = store.getCurrentSnapshot();

        store.enqueueChange(new WorldChange.EntityStateChange(id, state2));
        store.commitTick();
        WorldSnapshot snap2 = store.getCurrentSnapshot();

        assertEquals(loc1, snap1.entities().get(id).get().position());
        assertEquals(loc2, snap2.entities().get(id).get().position());
    }

    @Test
    void withEntityProducesNewSnapshotWithoutMutatingOriginal() {
        EntityId id = EntityId.of(99L);
        Location loc = new Location(5, 5, 5);
        WorldSnapshot original = store.getCurrentSnapshot();
        WorldSnapshot modified = original.withEntity(id, new EntityState(id, loc, Map.of()));

        assertFalse(original.entities().containsKey(id));
        assertTrue(modified.entities().containsKey(id));
    }

    @Test
    void queryPerceptionScopeExcludesEntitiesOutsideRadius() {
        EntityId nearby = EntityId.of(1L);
        EntityId farAway = EntityId.of(2L);

        store.enqueueChange(new WorldChange.EntityStateChange(nearby,
            new EntityState(nearby, new Location(10, 0, 0), Map.of())));
        store.enqueueChange(new WorldChange.EntityStateChange(farAway,
            new EntityState(farAway, new Location(200, 0, 0), Map.of())));
        store.commitTick();

        EntityId agent = EntityId.of(0L);
        QueryScope scope = QueryScope.perception(new Location(0, 0, 0));
        WorldFacts facts = store.query(agent, scope);

        assertTrue(facts.nearbyActors().contains(nearby));
        assertFalse(facts.nearbyActors().contains(farAway));
    }

    @Test
    void queryDialogueScopeIncludesRails() {
        NarrativeRails rails = new NarrativeRails("ACT_1", java.util.Set.of(), java.util.Set.of(), "NEUTRAL");
        store.enqueueChange(new WorldChange.NarrativeRailsChange(rails));
        store.commitTick();

        EntityId agent = EntityId.of(0L);
        WorldFacts facts = store.query(agent, QueryScope.dialogue(new Location(0, 0, 0)));
        assertNotNull(facts.rails());
        assertEquals("ACT_1", facts.rails().questStage());
    }

    @Test
    void queryPerceptionScopeExcludesRails() {
        NarrativeRails rails = new NarrativeRails("ACT_1", java.util.Set.of(), java.util.Set.of(), "NEUTRAL");
        store.enqueueChange(new WorldChange.NarrativeRailsChange(rails));
        store.commitTick();

        EntityId agent = EntityId.of(0L);
        WorldFacts facts = store.query(agent, QueryScope.perception(new Location(0, 0, 0)));
        assertNull(facts.rails());
    }

    @Test
    void circularBufferEvictsOldestAfter301Commits() {
        DefaultWorldStateStore smallStore = new DefaultWorldStateStore(300);

        for (int i = 0; i < 301; i++) {
            smallStore.commitTick();
        }

        assertNull(smallStore.getSnapshot(0L));
        assertNotNull(smallStore.getSnapshot(300L));
    }

    @Test
    void timeTravelReturnCorrectSnapshot() {
        store.commitTick();
        store.commitTick();
        WorldSnapshot snap2 = store.getCurrentSnapshot();
        store.commitTick();

        WorldSnapshot retrieved = store.getSnapshot(2L);
        assertNotNull(retrieved);
        assertEquals(2L, retrieved.tick());
        assertEquals(snap2.deterministicSeed(), retrieved.deterministicSeed());
    }

    @Test
    void concurrentEnqueueProducesConsistentSnapshot() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long entityId = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    EntityId id = EntityId.of(entityId);
                    store.enqueueChange(new WorldChange.EntityStateChange(id,
                        new EntityState(id, new Location(entityId, 0, 0), Map.of())));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        store.commitTick();
        WorldSnapshot snap = store.getCurrentSnapshot();

        assertEquals(10, snap.entities().size());
    }
}
