package org.dynamisengine.ai.perception;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.WorldStateStore;

/**
 * Per-frame perception pipeline.
 * Queries WorldStateStore, applies SaliencyFilter, caches the result.
 * Registered as a BudgetGovernor task — runs once per tick at HIGH priority.
 */
public interface PerceptionSystem {

    /**
     * Run one perception tick for an NPC.
     * Queries WorldStateStore via tactical QueryScope, runs SaliencyFilter.
     */
    PerceptionSnapshot tick(EntityId owner, AffectVector currentMood,
                            WorldStateStore store);

    /** Get the most recent snapshot without re-running perception. */
    PerceptionSnapshot getLastSnapshot(EntityId owner);
}
