package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;

public record BtContext(
    EntityId agent,
    WorldState worldState,
    SquadBlackboard squadBlackboard,
    long tick,
    long deterministicSeed
) {}
