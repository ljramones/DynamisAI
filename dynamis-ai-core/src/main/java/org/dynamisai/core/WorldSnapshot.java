package org.dynamisai.core;

import io.vavr.collection.HashMap;

public record WorldSnapshot(
    long tick,
    HashMap<EntityId, EntityState> entities,
    GlobalFacts globalFacts,
    EnvironmentState environment,
    long deterministicSeed
) {
    /** Per-NPC deterministic seed — same EntityId + same tick = same seed, always. */
    public long seedFor(EntityId id) {
        return Long.hashCode(tick) ^ id.value();
    }

    /** Structural sharing — O(log N), never clones the full map. */
    public WorldSnapshot withEntity(EntityId id, EntityState state) {
        return new WorldSnapshot(tick, entities.put(id, state),
            globalFacts, environment, deterministicSeed);
    }

    /** Produce the next tick snapshot — increments tick, derives new seed. */
    public WorldSnapshot nextTick(HashMap<EntityId, EntityState> updatedEntities,
                                   GlobalFacts updatedFacts,
                                   EnvironmentState updatedEnvironment) {
        long newTick = tick + 1;
        return new WorldSnapshot(newTick, updatedEntities,
            updatedFacts, updatedEnvironment, Long.hashCode(newTick));
    }
}
