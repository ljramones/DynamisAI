package org.dynamisai.planning;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A single decomposition method for a CompoundTask.
 * A method is applicable when its precondition holds.
 * Returns the ordered list of subtasks to pursue.
 */
public record DecompositionMethod(
    String methodId,
    Predicate<WorldState> precondition,
    List<HtnTask> subtasks
) {
    public boolean isApplicable(WorldState state) {
        return precondition.test(state);
    }

    public static Optional<DecompositionMethod> firstApplicable(
            List<DecompositionMethod> methods, WorldState state) {
        return methods.stream().filter(m -> m.isApplicable(state)).findFirst();
    }
}
