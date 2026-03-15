package org.dynamisengine.ai.cognition;

public record BeliefDecayPolicy(
    float decayPerTick,
    long stalenessThresholdTicks,
    float minimumRetainedConfidence
) {
    public static BeliefDecayPolicy defaultPolicy() {
        return new BeliefDecayPolicy(0.001f, 300L, 0.05f);
    }

    public static BeliefDecayPolicy fastDecay() {
        return new BeliefDecayPolicy(0.01f, 60L, 0.1f);
    }

    public static BeliefDecayPolicy persistent() {
        return new BeliefDecayPolicy(0.0001f, 3000L, 0.01f);
    }
}
