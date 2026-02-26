package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;

/**
 * Steering output for one NPC — produced by NavigationSystem each tick.
 * Consumed by HTN planner movement primitives and Animis (for locomotion blending).
 */
public record SteeringOutput(
    EntityId agent,
    NavPoint desiredVelocity,
    NavPoint lookDirection,
    float speed,
    NavPoint nextWaypoint,
    float distanceToGoal,
    boolean isAtGoal
) {
    private static final float ARRIVAL_RADIUS = 0.5f;

    public static SteeringOutput idle(EntityId agent, NavPoint position) {
        return new SteeringOutput(agent,
            NavPoint.of(0, 0, 0), NavPoint.of(0, 0, 1),
            0f, position, 0f, true);
    }

    public static SteeringOutput toward(EntityId agent, NavPoint position,
                                        NavPoint target, float speed,
                                        float distanceToGoal) {
        NavPoint dir = position.directionTo(target);
        NavPoint vel = NavPoint.of(dir.x() * speed, 0, dir.z() * speed);
        boolean atGoal = distanceToGoal < ARRIVAL_RADIUS;
        return new SteeringOutput(agent, vel, dir, speed, target, distanceToGoal, atGoal);
    }
}
