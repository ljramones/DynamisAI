package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.navigation.NavPoint;

/**
 * Per-agent state within a CrowdGroup.
 */
public record CrowdAgent(
    EntityId id,
    NavPoint position,
    NavPoint velocity,
    int slotIndex,
    boolean isLeader,
    float separationRadius
) {
    public CrowdAgent {
        if (separationRadius <= 0) {
            throw new IllegalArgumentException("separationRadius must be > 0");
        }
    }

    public static CrowdAgent of(EntityId id, NavPoint position) {
        return new CrowdAgent(id, position, NavPoint.of(0, 0, 0),
            -1, false, 0.6f);
    }

    public CrowdAgent withPosition(NavPoint pos) {
        return new CrowdAgent(id, pos, velocity, slotIndex, isLeader, separationRadius);
    }

    public CrowdAgent withVelocity(NavPoint vel) {
        return new CrowdAgent(id, position, vel, slotIndex, isLeader, separationRadius);
    }

    public CrowdAgent withSlot(int slot, boolean leader) {
        return new CrowdAgent(id, position, velocity, slot, leader, separationRadius);
    }
}
