package org.dynamisai.lod;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.LodTier;

public record ImportanceScore(
    EntityId entityId,
    float score,
    LodTier assignedTier,
    long computedAtTick
) {
}
