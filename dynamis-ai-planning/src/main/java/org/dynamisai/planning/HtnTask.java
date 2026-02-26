package org.dynamisai.planning;

import java.util.List;
import java.util.function.Predicate;

/**
 * Sealed HTN task hierarchy.
 * CompoundTask decomposes into subtasks via methods.
 * PrimitiveTask is a concrete executable action.
 * ConditionalTask branches on a WorldState predicate.
 */
public sealed interface HtnTask
    permits HtnTask.CompoundTask,
            HtnTask.PrimitiveTask,
            HtnTask.ConditionalTask {

    String taskId();

    record CompoundTask(
        String taskId,
        String description,
        List<DecompositionMethod> methods
    ) implements HtnTask {}

    record PrimitiveTask(
        String taskId,
        String description,
        Predicate<WorldState> precondition,
        List<PlannerEffect> effects,
        float cost,
        Runnable operator
    ) implements HtnTask {
        public boolean canExecute(WorldState state) {
            return precondition.test(state);
        }
    }

    record ConditionalTask(
        String taskId,
        Predicate<WorldState> condition,
        HtnTask ifTrue,
        HtnTask ifFalse
    ) implements HtnTask {
        public HtnTask evaluate(WorldState state) {
            return condition.test(state) ? ifTrue : ifFalse;
        }
    }
}
