package org.dynamisai.core;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldSnapshotTest {

    @Test
    void deterministicSeedIsNonZeroForNonZeroTick() {
        WorldSnapshot snapshot = new WorldSnapshot(
            5L,
            HashMap.empty(),
            new GlobalFacts(java.util.Map.of()),
            new EnvironmentState("clear", 12.0f, 1.0f)
        );
        assertNotEquals(0L, snapshot.deterministicSeed());
    }

    @Test
    void deterministicSeedConsistentForSameTick() {
        WorldSnapshot a = new WorldSnapshot(
            10L,
            HashMap.empty(),
            new GlobalFacts(java.util.Map.of()),
            new EnvironmentState("clear", 12.0f, 1.0f)
        );
        WorldSnapshot b = new WorldSnapshot(
            10L,
            HashMap.empty(),
            new GlobalFacts(java.util.Map.of()),
            new EnvironmentState("rain", 8.0f, 0.7f)
        );
        assertEquals(a.deterministicSeed(), b.deterministicSeed());
    }
}

