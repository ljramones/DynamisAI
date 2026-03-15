package org.dynamisengine.ai.core;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeterminismSeedManagerTest {

    @Test
    void sameEntityAndTickProducesSameSeed() {
        EntityId id = EntityId.of(42L);
        long a = DeterminismSeedManager.seedFor(id, 100L);
        long b = DeterminismSeedManager.seedFor(id, 100L);
        assertEquals(a, b);
    }

    @Test
    void differentEntitiesSameTickProduceDifferentSeeds() {
        long a = DeterminismSeedManager.seedFor(EntityId.of(1L), 100L);
        long b = DeterminismSeedManager.seedFor(EntityId.of(2L), 100L);
        assertNotEquals(a, b);
    }

    @Test
    void sameEntityDifferentTicksProduceDifferentSeeds() {
        EntityId id = EntityId.of(77L);
        long a = DeterminismSeedManager.seedFor(id, 100L);
        long b = DeterminismSeedManager.seedFor(id, 101L);
        assertNotEquals(a, b);
    }

    @Test
    void snapshotSeedIsConsistent() {
        long a = DeterminismSeedManager.seedForSnapshot(500L);
        long b = DeterminismSeedManager.seedForSnapshot(500L);
        assertEquals(a, b);
    }

    @Test
    void entitySeedDiffersFromSnapshotSeedAtSameTick() {
        long tick = 333L;
        long entitySeed = DeterminismSeedManager.seedFor(EntityId.of(9L), tick);
        long snapshotSeed = DeterminismSeedManager.seedForSnapshot(tick);
        assertNotEquals(entitySeed, snapshotSeed);
    }

    @Test
    void seedDistributionForHundredEntitiesHasNoDuplicates() {
        Set<Long> seeds = new HashSet<>();
        long tick = 1000L;
        for (int i = 1; i <= 100; i++) {
            seeds.add(DeterminismSeedManager.seedFor(EntityId.of(i), tick));
        }
        assertEquals(100, seeds.size());
    }
}
