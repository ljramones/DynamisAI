package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;

import java.util.Objects;

public record ReputationEvent(
    EntityId actor,
    EntityId target,
    ReputationEventType type,
    float magnitude,
    long tick,
    boolean isWitnessed,
    EntityId witness
) {
    public ReputationEvent {
        Objects.requireNonNull(actor);
        Objects.requireNonNull(target);
        Objects.requireNonNull(type);
        if (magnitude < 0f || magnitude > 1f) {
            throw new IllegalArgumentException("magnitude must be [0,1]");
        }
    }
}
