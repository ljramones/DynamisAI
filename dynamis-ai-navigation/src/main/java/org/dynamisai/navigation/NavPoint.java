package org.dynamisai.navigation;

import org.dynamisai.core.Location;

/**
 * A 3D point in navigation space.
 * Navigation is computed in 3D but steering output is applied in world space.
 */
public record NavPoint(float x, float y, float z) {

    public static NavPoint of(float x, float y, float z) {
        return new NavPoint(x, y, z);
    }

    public static NavPoint from(Location loc) {
        return new NavPoint(loc.x(), loc.y(), loc.z());
    }

    public Location toLocation() {
        return new Location(x, y, z);
    }

    public float distanceTo(NavPoint other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceToXZ(NavPoint other) {
        float dx = x - other.x;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    /** Linear interpolation toward other by factor t in [0,1]. */
    public NavPoint lerp(NavPoint other, float t) {
        return new NavPoint(
            x + (other.x - x) * t,
            y + (other.y - y) * t,
            z + (other.z - z) * t);
    }

    /** Unit vector from this to other. Returns NavPoint.of(0,0,0) if coincident. */
    public NavPoint directionTo(NavPoint other) {
        float dist = distanceTo(other);
        if (dist < 1e-6f) {
            return NavPoint.of(0, 0, 0);
        }
        return new NavPoint(
            (other.x - x) / dist,
            (other.y - y) / dist,
            (other.z - z) / dist);
    }
}
