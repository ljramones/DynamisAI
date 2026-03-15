package org.dynamisengine.ai.voice;

import java.util.Map;

/**
 * Data-driven table mapping visemes and affect dimensions to blendshape weights.
 */
public record BlendshapeTable(
    Map<String, Map<String, Float>> visemeWeights,
    Map<String, Float> affectValencePos,
    Map<String, Float> affectValenceNeg,
    Map<String, Float> affectArousal,
    Map<String, Float> affectDominance,
    Map<String, Float> affectSarcasm
) {
    public static BlendshapeTable defaultHumanoid() {
        Map<String, Map<String, Float>> visemes = Map.ofEntries(
            Map.entry("rest", Map.of("jawOpen", 0.0f, "mouthClose", 0.8f)),
            Map.entry("MBP", Map.of("jawOpen", 0.0f, "mouthClose", 1.0f, "mouthPressLip", 0.6f)),
            Map.entry("etc", Map.of("jawOpen", 0.15f, "mouthStretchL", 0.2f, "mouthStretchR", 0.2f)),
            Map.entry("AI", Map.of("jawOpen", 0.7f, "mouthStretchL", 0.5f, "mouthStretchR", 0.5f)),
            Map.entry("O", Map.of("jawOpen", 0.5f, "mouthFunnel", 0.8f)),
            Map.entry("E", Map.of("jawOpen", 0.3f, "mouthStretchL", 0.7f, "mouthStretchR", 0.7f)),
            Map.entry("U", Map.of("jawOpen", 0.2f, "mouthPucker", 0.9f)),
            Map.entry("WQ", Map.of("jawOpen", 0.3f, "mouthFunnel", 0.5f, "mouthPucker", 0.4f)),
            Map.entry("FV", Map.of("jawOpen", 0.1f, "mouthLowerDownL", 0.4f, "mouthLowerDownR", 0.4f)),
            Map.entry("L", Map.of("jawOpen", 0.3f, "tongueOut", 0.4f)),
            Map.entry("Th", Map.of("jawOpen", 0.2f, "tongueOut", 0.7f))
        );

        return new BlendshapeTable(
            visemes,
            Map.of("mouthSmileL", 1.0f, "mouthSmileR", 1.0f, "cheekSquintL", 0.3f, "cheekSquintR", 0.3f),
            Map.of("mouthFrownL", 1.0f, "mouthFrownR", 1.0f, "browInnerUp", 0.4f),
            Map.of("browOuterUpL", 0.8f, "browOuterUpR", 0.8f, "eyeWideL", 0.5f, "eyeWideR", 0.5f),
            Map.of("browDownL", 0.7f, "browDownR", 0.7f, "noseSneerL", 0.3f, "noseSneerR", 0.3f),
            Map.of("mouthSmileL", 0.6f, "mouthFrownR", 0.3f, "browOuterUpL", 0.5f)
        );
    }
}
