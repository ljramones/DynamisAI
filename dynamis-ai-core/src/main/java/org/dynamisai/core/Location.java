package org.dynamisai.core;

/** Stub location — Vectrix integration replaces this in a later task. */
public record Location(float x, float y, float z) {
    public float distanceTo(Location other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Bearing in degrees on the XZ plane with 0=north (-Z), clockwise positive.
     * Example: east is 90, south is 180, west is 270.
     */
    public float bearingTo(Location other) {
        float dx = other.x - this.x;
        float dz = other.z - this.z;
        double radians = Math.atan2(dx, -dz);
        float degrees = (float) Math.toDegrees(radians);
        return (degrees % 360f + 360f) % 360f;
    }
}
