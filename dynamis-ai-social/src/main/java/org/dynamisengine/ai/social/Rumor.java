package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;

import java.util.UUID;

public record Rumor(
    UUID id,
    ReputationEvent sourceEvent,
    EntityId currentHolder,
    EntityId origin,
    int hopCount,
    float fidelity,
    long createdAtTick,
    long propagatedAtTick
) {
    public static final float FIDELITY_DECAY_PER_HOP = 0.15f;

    public Rumor {
        if (fidelity < 0f || fidelity > 1f) {
            throw new IllegalArgumentException("fidelity must be [0,1]");
        }
    }

    public Rumor propagateTo(EntityId nextHolder, long tick) {
        return new Rumor(
            id,
            sourceEvent,
            nextHolder,
            origin,
            hopCount + 1,
            Math.max(0f, fidelity - FIDELITY_DECAY_PER_HOP),
            createdAtTick,
            tick
        );
    }
}
