package org.dynamisai.core;

/** Stub location — Vectrix integration replaces this in a later task. */
public record Location(float x, float y, float z) {
    public float distanceTo(Location other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
