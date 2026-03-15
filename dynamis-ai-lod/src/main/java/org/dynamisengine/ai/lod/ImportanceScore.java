package org.dynamisengine.ai.lod;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.LodTier;

public record ImportanceScore(
    EntityId entityId,
    float score,
    LodTier assignedTier,
    long computedAtTick
) {
}
