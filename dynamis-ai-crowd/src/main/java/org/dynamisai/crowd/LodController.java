package org.dynamisai.crowd;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.LodTier;
import org.dynamisai.navigation.NavPoint;

import java.util.Map;

/**
 * Assigns CrowdLod levels to groups based on distance to the observer.
 */
public final class LodController {

    private final float fullMaxDist;
    private final float reducedMaxDist;
    private final float skeletonMaxDist;

    private volatile NavPoint observer = NavPoint.of(0, 0, 0);
    private volatile Map<EntityId, LodTier> aiPolicyTiers = Map.of();

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
        CrowdLod policyLod = crowdLodFromPolicy(group);
        if (policyLod != null) {
            return policyLod;
        }
        float dist = observer.distanceTo(group.centroid());
        if (dist < fullMaxDist) return CrowdLod.FULL;
        if (dist < reducedMaxDist) return CrowdLod.REDUCED;
        if (dist < skeletonMaxDist) return CrowdLod.SKELETON;
        return CrowdLod.CULLED;
    }

    public NavPoint observer() { return observer; }

    /**
     * Optionally sync external AI LOD tier assignments for entity-level overrides.
     */
    public void syncFromAILODPolicy(Map<EntityId, LodTier> policyTiers) {
        this.aiPolicyTiers = policyTiers == null ? Map.of() : Map.copyOf(policyTiers);
    }

    private CrowdLod crowdLodFromPolicy(CrowdGroup group) {
        CrowdLod best = null;
        for (CrowdAgent agent : group.agents()) {
            LodTier tier = aiPolicyTiers.get(agent.id());
            if (tier == null) {
                continue;
            }
            CrowdLod mapped = switch (tier) {
                case TIER_0 -> CrowdLod.FULL;
                case TIER_1 -> CrowdLod.REDUCED;
                case TIER_2 -> CrowdLod.SKELETON;
                case TIER_3 -> CrowdLod.CULLED;
            };
            if (best == null || mapped.ordinal() < best.ordinal()) {
                best = mapped;
            }
        }
        return best;
    }
}
