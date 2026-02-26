package org.dynamisai.lod;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.LodTier;

public record ImportanceScore(
    EntityId entityId,
    float score,
    LodTier assignedTier,
    long computedAtTick
) {
}
