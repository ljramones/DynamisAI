package org.dynamisai.core;

/**
 * Bridge contract consumed by BudgetGovernor for LOD-gated execution.
 */
public interface AILODPolicy {

    /**
     * Whether AI for the entity should execute on this tick.
     */
    boolean shouldRunAi(EntityId entityId, long currentTick, WorldSnapshot snapshot);
}

