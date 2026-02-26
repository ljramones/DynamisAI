package org.dynamisai.planning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public record GoapAction(
    String name,
    Predicate<WorldState> precondition,
    List<PlannerEffect> effects,
    float baseCost,
    ToDoubleFunction<WorldState> dynamicCost
) {
    public GoapAction {
        Objects.requireNonNull(name);
        Objects.requireNonNull(precondition);
        Objects.requireNonNull(effects);
        if (baseCost < 0f) {
            throw new IllegalArgumentException("baseCost must be >= 0");
        }
        effects = List.copyOf(effects);
    }

    public float cost(WorldState state) {
        return dynamicCost != null
            ? (float) dynamicCost.applyAsDouble(state)
            : baseCost;
    }

    public WorldState apply(WorldState state) {
        if (effects.isEmpty()) {
            return state;
        }
        Map<String, Object> updated = new HashMap<>(state.blackboard());
        for (PlannerEffect effect : effects) {
            updated.put(effect.key(), effect.value());
        }
        return new WorldState(
            state.owner(), state.tick(), state.affect(),
            state.currentThreat(), state.perception(),
            state.memoryStats(), Map.copyOf(updated),
            state.agentPosition(), state.goalPosition(), state.distanceToGoal()
        );
    }

    public static GoapAction of(String name,
                                Predicate<WorldState> pre,
                                List<PlannerEffect> effects,
                                float cost) {
        return new GoapAction(name, pre, effects, cost, null);
    }
}
