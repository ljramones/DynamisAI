package org.dynamisengine.ai.perception;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;

/**
 * A single sensory percept — one thing the NPC noticed this frame.
 * Immutable. Salience score is computed by SaliencyFilter, never authored directly.
 */
public record Percept(
    EntityId source,
    StimulusType stimulusType,
    Location location,
    float rawIntensity,
    float salienceScore,
    ThreatLevel perceivedThreat,
    boolean isNovel
) {
    public Percept {
        if (rawIntensity < 0f || rawIntensity > 1f)
            throw new IllegalArgumentException("rawIntensity must be in [0,1]");
        if (salienceScore < 0f || salienceScore > 1f)
            throw new IllegalArgumentException("salienceScore must be in [0,1]");
    }
}
