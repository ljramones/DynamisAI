package org.dynamisai.perception;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;

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
