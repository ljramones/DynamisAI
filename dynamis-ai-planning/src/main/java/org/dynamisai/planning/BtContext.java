package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;

public record BtContext(
    EntityId agent,
    WorldState worldState,
    SquadBlackboard squadBlackboard,
    long tick,
    long deterministicSeed
) {}
