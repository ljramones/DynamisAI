package org.dynamisengine.ai.perception;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;

import java.util.Objects;

/** Discrete sound stimulus queued for simulated hearing. */
public record SoundEvent(
    EntityId sourceEntityId,
    Location position,
    float intensity,
    StimulusType type,
    long tick
) {
    public SoundEvent {
        Objects.requireNonNull(position);
        Objects.requireNonNull(type);
        if (intensity < 0f || intensity > 1f) {
            throw new IllegalArgumentException("intensity must be [0,1]");
        }
    }
}
