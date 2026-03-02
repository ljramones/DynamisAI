package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.BeliefSource;

import java.util.Objects;

public record Belief(
    String key,
    Object value,
    float confidence,
    long formedAtTick,
    long lastReinforcedAtTick,
    BeliefSource source,
    EntityId holder
) {
    public Belief {
        Objects.requireNonNull(key);
        Objects.requireNonNull(source);
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
            source,
            holder);
    }

    public Belief reinforced(float newConfidence, long tick) {
        return new Belief(
            key,
            value,
            Math.min(1f, Math.max(0f, newConfidence)),
            formedAtTick,
            tick,
            source,
            holder);
    }

    public boolean isStale(long currentTick, long stalenessThreshold) {
        return (currentTick - lastReinforcedAtTick) > stalenessThreshold;
    }
}
