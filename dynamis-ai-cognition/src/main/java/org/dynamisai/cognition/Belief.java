package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;

import java.util.Objects;

public record Belief(
    String key,
    Object value,
    float confidence,
    long formedAtTick,
    long lastReinforcedAtTick,
    EntityId holder
) {
    public Belief {
        Objects.requireNonNull(key);
        Objects.requireNonNull(holder);
        if (confidence < 0f || confidence > 1f) {
            throw new IllegalArgumentException("confidence must be [0,1]");
        }
    }

    public Belief decayed(float decayAmount) {
        return new Belief(
            key,
            value,
            Math.max(0f, confidence - decayAmount),
            formedAtTick,
            lastReinforcedAtTick,
            holder);
    }

    public Belief reinforced(float newConfidence, long tick) {
        return new Belief(
            key,
            value,
            Math.min(1f, Math.max(0f, newConfidence)),
            formedAtTick,
            tick,
            holder);
    }

    public boolean isStale(long currentTick, long stalenessThreshold) {
        return (currentTick - lastReinforcedAtTick) > stalenessThreshold;
    }
}
