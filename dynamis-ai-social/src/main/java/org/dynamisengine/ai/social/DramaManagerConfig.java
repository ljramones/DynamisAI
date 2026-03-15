package org.dynamisengine.ai.social;

public record DramaManagerConfig(
    float escalationThreshold,
    float reliefThreshold,
    float tensionThreshold,
    float lullThreshold,
    int minTicksBetweenBeatChanges,
    float maxAggressionMagnitude
) {
    public DramaManagerConfig {
        escalationThreshold = clamp01(escalationThreshold);
        reliefThreshold = clamp01(reliefThreshold);
        tensionThreshold = clamp01(tensionThreshold);
        lullThreshold = clamp01(lullThreshold);
        maxAggressionMagnitude = clamp01(maxAggressionMagnitude);
        if (minTicksBetweenBeatChanges < 0) {
            throw new IllegalArgumentException("minTicksBetweenBeatChanges must be >= 0");
        }
    }

    public static DramaManagerConfig defaultConfig() {
        return new DramaManagerConfig(0.7f, 0.3f, 0.4f, 0.2f, 120, 0.8f);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
