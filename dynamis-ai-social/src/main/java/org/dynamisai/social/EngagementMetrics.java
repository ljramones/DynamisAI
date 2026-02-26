package org.dynamisai.social;

public record EngagementMetrics(
    float combatIntensity,
    float explorationRate,
    float decisionSpeed,
    float idleTime,
    float threatPressure,
    long windowTicks,
    long computedAtTick
) {
    public EngagementMetrics {
        combatIntensity = clamp01(combatIntensity);
        explorationRate = clamp01(explorationRate);
        decisionSpeed = clamp01(decisionSpeed);
        idleTime = clamp01(idleTime);
        threatPressure = clamp01(threatPressure);
    }

    public static EngagementMetrics neutral(long tick) {
        return new EngagementMetrics(0.5f, 0.5f, 0.5f, 0f, 0.3f, 300L, tick);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
