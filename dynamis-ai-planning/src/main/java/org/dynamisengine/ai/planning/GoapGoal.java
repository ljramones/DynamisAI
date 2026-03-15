package org.dynamisengine.ai.planning;

import java.util.Objects;
import java.util.function.Predicate;

public record GoapGoal(
    String name,
    Predicate<WorldState> isSatisfied,
    float priority
) {
    public GoapGoal {
        Objects.requireNonNull(name);
        Objects.requireNonNull(isSatisfied);
        if (priority < 0f || priority > 1f) {
            throw new IllegalArgumentException("priority must be [0,1]");
        }
    }
}
