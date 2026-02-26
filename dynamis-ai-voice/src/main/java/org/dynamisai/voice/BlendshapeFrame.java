package org.dynamisai.voice;

/**
 * Blendshape state at a single point in time.
 */
public record BlendshapeFrame(
    float timeSeconds,
    BlendshapeWeights weights
) {}
