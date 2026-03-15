package org.dynamisengine.ai.core;

import org.dynamisengine.core.entity.EntityId;

/**
 * Deterministic seed derivation utilities.
 * Pure static functions with no mutable state.
 */
public final class DeterminismSeedManager {

    private static final long FNV_OFFSET_BASIS_64 = 0xcbf29ce484222325L;
    private static final long FNV_PRIME_64 = 0x100000001b3L;
    private static final long SNAPSHOT_DOMAIN = 0x53A9D35EEDL;
    private static final long ENTITY_DOMAIN = 0xE17A7C1DL;

    private DeterminismSeedManager() {
    }

    /**
     * Same entity + same tick => same seed.
     * Different entity or tick => different seed with high probability.
     */
    public static long seedFor(EntityId entityId, long snapshotTick) {
        long hash = FNV_OFFSET_BASIS_64;
        hash = mix(hash, ENTITY_DOMAIN);
        hash = mix(hash, entityId.id());
        hash = mix(hash, snapshotTick);
        return hash;
    }

    /**
     * Snapshot-level deterministic seed.
     */
    public static long seedForSnapshot(long snapshotTick) {
        long hash = FNV_OFFSET_BASIS_64;
        hash = mix(hash, SNAPSHOT_DOMAIN);
        hash = mix(hash, snapshotTick);
        return hash;
    }

    private static long mix(long hash, long value) {
        hash ^= value;
        hash *= FNV_PRIME_64;
        return hash;
    }
}
