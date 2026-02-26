package org.dynamisai.planning;

import org.dynamisai.core.EntityId;

public record BtContext(
    EntityId agent,
    WorldState worldState,
    SquadBlackboard squadBlackboard,
    long tick,
    long deterministicSeed
) {}
