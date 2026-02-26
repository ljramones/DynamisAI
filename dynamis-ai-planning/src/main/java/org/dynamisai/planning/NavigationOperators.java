package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.navigation.NavigationSystem;
import org.dynamisai.navigation.PathRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for Runnable operators that delegate to NavigationSystem.
 */
public final class NavigationOperators {

    private static final Logger log = LoggerFactory.getLogger(NavigationOperators.class);

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
            log.debug("moveTo operator fired for {} -> {}", agent, goal);
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
            log.debug("fleeFrom operator fired for {} away from {}", agent, threatPos);
        };
    }

    /**
     * Operator: stop all movement for this agent.
     */
    public static Runnable stop(NavigationSystem nav, EntityId agent) {
        return () -> {
            nav.removeAgent(agent);
            log.debug("stop operator fired for {}", agent);
        };
    }
}
