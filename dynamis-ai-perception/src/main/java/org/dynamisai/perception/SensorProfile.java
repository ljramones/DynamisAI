package org.dynamisai.perception;

/** Per-entity sensing parameters used by SimulatedSenses. */
public record SensorProfile(
    float visionRadiusMetres,
    float visionAngleDegrees,
    float hearingRadiusMetres,
    float hearingAcuity,
    float facingAngleDegrees
) {
    public SensorProfile {
        if (visionRadiusMetres < 0f) {
            throw new IllegalArgumentException("visionRadius must be >= 0");
        }
        if (visionAngleDegrees < 0f || visionAngleDegrees > 360f) {
            throw new IllegalArgumentException("visionAngle must be [0,360]");
        }
        if (hearingRadiusMetres < 0f) {
            throw new IllegalArgumentException("hearingRadius must be >= 0");
        }
        if (hearingAcuity < 0f || hearingAcuity > 1f) {
            throw new IllegalArgumentException("hearingAcuity must be [0,1]");
        }
    }

    public static SensorProfile defaultHuman() {
        return new SensorProfile(30f, 120f, 20f, 0.5f, 0f);
    }

    public static SensorProfile blind() {
        return new SensorProfile(0f, 0f, 20f, 0.5f, 0f);
    }

    public static SensorProfile deaf() {
        return new SensorProfile(30f, 120f, 0f, 0f, 0f);
    }
}
