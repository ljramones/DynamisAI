package org.dynamisai.voice;

/**
 * Spatial hint for motion matching — which direction the NPC is oriented toward.
 */
public record Direction(float x, float y, float z) {

    public static Direction forward() { return new Direction(0f, 0f, 1f); }
    public static Direction backward() { return new Direction(0f, 0f, -1f); }
    public static Direction left() { return new Direction(-1f, 0f, 0f); }
    public static Direction right() { return new Direction(1f, 0f, 0f); }
    public static Direction none() { return new Direction(0f, 0f, 0f); }

    /** Normalize to unit vector. Returns Direction.none() if magnitude is zero. */
    public Direction normalized() {
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        if (mag < 1e-6f) return none();
        return new Direction(x / mag, y / mag, z / mag);
    }
}
