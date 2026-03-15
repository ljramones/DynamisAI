package org.dynamisengine.ai.planning;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

public record UtilityAction(
    String name,
    ToDoubleFunction<WorldState> scorer,
    Runnable onSelected
) {
    public UtilityAction {
        Objects.requireNonNull(name);
        Objects.requireNonNull(scorer);
    }

    public double score(WorldState state) {
        return Math.max(0.0, Math.min(1.0, scorer.applyAsDouble(state)));
    }
}
