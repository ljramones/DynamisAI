package org.dynamisai.planning;

/**
 * HTN planner interface.
 * Called from deliberative tick (~1 Hz) — never from reactive layer.
 * Returns a Plan of ordered PrimitiveTasks ready for execution.
 */
public interface HtnPlanner {

    /**
     * Produce a plan for the given root task and world state.
     */
    Plan plan(HtnTask rootTask, WorldState state, PlanningBudget budget);
}
