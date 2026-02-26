package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;

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
