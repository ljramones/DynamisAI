package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;

import java.util.concurrent.CompletableFuture;

/**
 * Navigation system interface.
 * Pathfinding is async — results delivered via CompletableFuture.
 * Steering output is synchronous — called each tick from BudgetGovernor task.
 */
public interface NavigationSystem {

    /**
     * Request an async path from start to goal.
     * Runs on virtual thread — never blocks simulation thread.
     */
    CompletableFuture<PathResult> requestPath(PathRequest request);

    /**
     * Compute steering output for an agent given its current path.
     * Synchronous — called from HIGH priority BudgetGovernor task.
     * Returns SteeringOutput.idle() if no path is available.
     *
     * @param agent    The NPC
     * @param position Current world position
     * @param speed    Desired movement speed
     */
    SteeringOutput steer(EntityId agent, Location position, float speed);

    /**
     * Update agent velocity state for RVO2 avoidance.
     * Call after steer() for all agents in the local neighbourhood.
     */
    void updateAgentState(RvoAgent agent);

    /**
     * Remove all navigation state for an agent — call on despawn.
     */
    void removeAgent(EntityId agent);

    /** True if NavigationSystem has a NavMesh loaded. */
    boolean isReady();
}
