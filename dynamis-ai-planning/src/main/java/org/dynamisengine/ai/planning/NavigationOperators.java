package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.navigation.PathRequest;
import org.dynamisengine.core.logging.DynamisLogger;

/**
 * Factory for Runnable operators that delegate to NavigationSystem.
 */
public final class NavigationOperators {

    private static final DynamisLogger log = DynamisLogger.get(NavigationOperators.class);

    private NavigationOperators() {}

    /**
     * Operator: request path to a fixed goal.
     * Fires async path request — returns immediately.
     */
    public static Runnable moveTo(NavigationSystem nav,
                                  EntityId agent,
                                  Location goal) {
        return () -> {
            nav.requestPath(PathRequest.of(agent,
                new Location(0, 0, 0),
                goal));
            log.debug(String.format("moveTo operator fired for %s -> %s", agent, goal));
        };
    }

    /**
     * Operator: flee from a threat position.
     * Requests a path to a point directly opposite the threat.
     */
    public static Runnable fleeFrom(NavigationSystem nav,
                                    EntityId agent,
                                    Location agentPos,
                                    Location threatPos) {
        return () -> {
            float dx = (float) (agentPos.x() - threatPos.x());
            float dy = (float) (agentPos.y() - threatPos.y());
            float dz = (float) (agentPos.z() - threatPos.z());
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            float fleeDistance = 15f;
            Location fleeGoal = len < 0.01f
                ? new Location(agentPos.x() + fleeDistance, agentPos.y(), agentPos.z())
                : new Location(
                    agentPos.x() + (dx / len) * fleeDistance,
                    agentPos.y() + (dy / len) * fleeDistance,
                    agentPos.z() + (dz / len) * fleeDistance
                );
            nav.requestPath(PathRequest.of(agent, agentPos, fleeGoal));
            log.debug(String.format("fleeFrom operator fired for %s away from %s", agent, threatPos));
        };
    }

    /**
     * Operator: stop all movement for this agent.
     */
    public static Runnable stop(NavigationSystem nav, EntityId agent) {
        return () -> {
            nav.removeAgent(agent);
            log.debug(String.format("stop operator fired for %s", agent));
        };
    }
}
