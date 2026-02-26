package org.dynamisai.tools;

import org.dynamisai.cognition.AffectVector;

public record AffectRadarSnapshot(
    float fear,
    float suspicion,
    float curiosity,
    float aggression,
    float loyalty,
    long tick
) {
    public static AffectRadarSnapshot from(AffectVector affect,
                                           float loyaltyUrgency,
                                           long tick) {
        AffectVector a = affect == null ? AffectVector.neutral() : affect;
        float fear = a.valence() < 0f ? -a.valence() * a.intensity() : 0f;
        float suspicion = a.arousal() * (1f - a.dominance());
        float curiosity = a.valence() >= 0f ? a.arousal() * a.dominance() : 0f;
        float aggression = a.dominance() * a.intensity();
        return new AffectRadarSnapshot(
            clamp(fear), clamp(suspicion), clamp(curiosity),
            clamp(aggression), clamp(loyaltyUrgency), tick
        );
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
