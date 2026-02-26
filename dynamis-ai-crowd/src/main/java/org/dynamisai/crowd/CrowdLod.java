package org.dynamisai.crowd;

/**
 * Crowd Level of Detail — controls simulation fidelity for a CrowdGroup
 * based on distance to the observer.
 */
public enum CrowdLod {
    FULL,
    REDUCED,
    SKELETON,
    CULLED;

    /** Default distance thresholds in world units. */
    public static final float FULL_MAX_DIST = 20f;
    public static final float REDUCED_MAX_DIST = 60f;
    public static final float SKELETON_MAX_DIST = 150f;

    /**
     * Assign LOD based on distance from observer to group centroid.
     */
    public static CrowdLod forDistance(float distance) {
        if (distance < FULL_MAX_DIST) return FULL;
        if (distance < REDUCED_MAX_DIST) return REDUCED;
        if (distance < SKELETON_MAX_DIST) return SKELETON;
        return CULLED;
    }
}
