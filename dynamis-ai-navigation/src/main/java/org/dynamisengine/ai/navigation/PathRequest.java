package org.dynamisengine.ai.navigation;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;

/**
 * Async pathfinding request submitted to NavigationSystem.
 */
public record PathRequest(
    EntityId requester,
    Location from,
    Location to,
    int priority
) {
    public static PathRequest of(EntityId requester, Location from, Location to) {
        return new PathRequest(requester, from, to, 5);
    }
}
