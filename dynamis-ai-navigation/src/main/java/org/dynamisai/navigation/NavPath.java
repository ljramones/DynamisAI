package org.dynamisai.navigation;

import java.util.List;

/**
 * An ordered sequence of NavPoints from start to goal.
 * Produced by HpaPathfinder. Consumed by DefaultNavigationSystem.
 */
public record NavPath(
    List<NavPoint> waypoints,
    float totalCost,
    boolean isComplete
) {
    public static NavPath empty() {
        return new NavPath(List.of(), Float.MAX_VALUE, false);
    }

    public boolean hasWaypoints() {
        return !waypoints.isEmpty();
    }

    /** Next waypoint after the start — the immediate steering target. */
    public NavPoint nextWaypoint() {
        if (waypoints.size() < 2) {
            return waypoints.isEmpty() ? null : waypoints.get(0);
        }
        return waypoints.get(1);
    }

    /** True if start and goal are in the same poly — no waypoints needed. */
    public boolean isAtGoal() {
        return waypoints.size() <= 1;
    }
}
