package org.dynamisengine.ai.core;

/**
 * Per-tick steering output for one entity.
 */
public record SteeringOutput(
    Location desiredVelocity,
    Location lookDirection,
    float speed,
    boolean isAtGoal
) {
    public static SteeringOutput stopped() {
        return new SteeringOutput(
            new Location(0, 0, 0),
            new Location(0, 0, 1),
            0f,
            false
        );
    }

    public static SteeringOutput atGoal() {
        return new SteeringOutput(
            new Location(0, 0, 0),
            new Location(0, 0, 1),
            0f,
            true
        );
    }
}
