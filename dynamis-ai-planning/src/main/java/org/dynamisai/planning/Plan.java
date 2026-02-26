package org.dynamisai.planning;

import java.util.List;

/**
 * An ordered list of PrimitiveTasks ready for execution.
 * Output of HtnPlanner.plan(). Empty plan = no applicable decomposition found.
 */
public record Plan(
    List<HtnTask.PrimitiveTask> tasks,
    float totalCost,
    int decompositionDepth,
    boolean isComplete
) {
    public static Plan empty() {
        return new Plan(List.of(), 0f, 0, true);
    }

    public boolean hasTasks() {
        return !tasks.isEmpty();
    }
}
