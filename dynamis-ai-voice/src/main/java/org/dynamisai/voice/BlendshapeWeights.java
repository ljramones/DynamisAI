package org.dynamisai.voice;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable blendshape weights for one sample point.
 */
public record BlendshapeWeights(Map<String, Float> weights) {

    public BlendshapeWeights {
        Objects.requireNonNull(weights, "weights");
        weights = Map.copyOf(weights);
        for (float v : weights.values()) {
            if (v < 0f || v > 1f) {
                throw new IllegalArgumentException("Blendshape weight out of range [0,1]: " + v);
            }
        }
    }

    public float get(String name) {
        return weights.getOrDefault(name, 0f);
    }
}
