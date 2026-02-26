package org.dynamisai.planning;

/**
 * Planning constraints — prevents combinatorial explosion.
 * Hard limits enforced by DefaultHtnPlanner.
 */
public record PlanningBudget(
    int maxDepth,
    int maxNodes,
    long maxPlanningMs
) {
    public PlanningBudget {
        if (maxDepth <= 0) throw new IllegalArgumentException("maxDepth must be > 0");
        if (maxNodes <= 0) throw new IllegalArgumentException("maxNodes must be > 0");
        if (maxPlanningMs <= 0) throw new IllegalArgumentException("maxPlanningMs must be > 0");
    }

    public static PlanningBudget standard() {
        return new PlanningBudget(8, 64, 5L);
    }

    public static PlanningBudget tight() {
        return new PlanningBudget(4, 16, 2L);
    }
}
