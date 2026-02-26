package org.dynamisai.voice;

import java.time.Duration;

/**
 * A single phoneme keyframe in the lip-sync timeline.
 * Fed to Animis to drive blendshape weights.
 */
public record VisemeTimestamp(
    Duration offset,
    String viseme,
    float weight
) {
    public VisemeTimestamp {
        if (weight < 0f || weight > 1f) {
            throw new IllegalArgumentException("weight must be in [0,1]");
        }
        if (offset.isNegative()) {
            throw new IllegalArgumentException("offset must be non-negative");
        }
    }
}
