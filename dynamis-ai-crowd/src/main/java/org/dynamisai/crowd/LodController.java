package org.dynamisai.crowd;

import org.dynamisai.navigation.NavPoint;

/**
 * Assigns CrowdLod levels to groups based on distance to the observer.
 */
public final class LodController {

    private final float fullMaxDist;
    private final float reducedMaxDist;
    private final float skeletonMaxDist;

    private volatile NavPoint observer = NavPoint.of(0, 0, 0);

    public LodController() {
        this(CrowdLod.FULL_MAX_DIST,
             CrowdLod.REDUCED_MAX_DIST,
             CrowdLod.SKELETON_MAX_DIST);
    }

    public LodController(float fullMaxDist, float reducedMaxDist,
                          float skeletonMaxDist) {
        this.fullMaxDist = fullMaxDist;
        this.reducedMaxDist = reducedMaxDist;
        this.skeletonMaxDist = skeletonMaxDist;
    }

    /** Update observer position — call each tick from game thread. */
    public void setObserver(NavPoint observer) { this.observer = observer; }

    /** Assign LOD to a group based on its centroid distance to observer. */
    public CrowdLod assignLod(CrowdGroup group) {
        float dist = observer.distanceTo(group.centroid());
        if (dist < fullMaxDist) return CrowdLod.FULL;
        if (dist < reducedMaxDist) return CrowdLod.REDUCED;
        if (dist < skeletonMaxDist) return CrowdLod.SKELETON;
        return CrowdLod.CULLED;
    }

    public NavPoint observer() { return observer; }
}
