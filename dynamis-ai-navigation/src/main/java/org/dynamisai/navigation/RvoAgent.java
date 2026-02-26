package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;

/**
 * Agent state for RVO2 local avoidance computation.
 * All navigation is in XZ plane (Y is ignored for avoidance).
 * position and velocity are NavPoints — Y component is unused in ORCA.
 */
public record RvoAgent(
    EntityId id,
    NavPoint position,
    NavPoint velocity,
    NavPoint preferredVelocity,
    float radius,
    float maxSpeed
) {
    public RvoAgent {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        if (maxSpeed <= 0) {
            throw new IllegalArgumentException("maxSpeed must be > 0");
        }
    }

    public static RvoAgent of(EntityId id, NavPoint pos, float radius, float maxSpeed) {
        return new RvoAgent(id, pos, NavPoint.of(0, 0, 0), NavPoint.of(0, 0, 0), radius, maxSpeed);
    }

    public RvoAgent withVelocity(NavPoint vel) {
        return new RvoAgent(id, position, vel, preferredVelocity, radius, maxSpeed);
    }

    public RvoAgent withPreferredVelocity(NavPoint pref) {
        return new RvoAgent(id, position, velocity, pref, radius, maxSpeed);
    }

    public RvoAgent withPosition(NavPoint pos) {
        return new RvoAgent(id, pos, velocity, preferredVelocity, radius, maxSpeed);
    }
}
