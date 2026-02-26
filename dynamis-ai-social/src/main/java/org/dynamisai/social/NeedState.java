package org.dynamisai.social;

public record NeedState(
    NeedType type,
    float urgency,
    float decayRate,
    float growthRate
) {
    public NeedState {
        if (urgency < 0f || urgency > 1f) {
            throw new IllegalArgumentException("urgency must be [0,1]");
        }
        if (decayRate < 0f) {
            throw new IllegalArgumentException("decayRate must be >= 0");
        }
        if (growthRate < 0f) {
            throw new IllegalArgumentException("growthRate must be >= 0");
        }
    }

    public NeedState withUrgency(float newUrgency) {
        return new NeedState(type, clamp01(newUrgency), decayRate, growthRate);
    }

    public NeedState tick(boolean isMet) {
        float delta = isMet ? -decayRate : growthRate;
        return withUrgency(urgency + delta);
    }

    public static NeedState defaultFor(NeedType type) {
        return switch (type) {
            case SAFETY -> new NeedState(type, 0.1f, 0.05f, 0.02f);
            case STATUS -> new NeedState(type, 0.3f, 0.01f, 0.005f);
            case GREED -> new NeedState(type, 0.2f, 0.02f, 0.008f);
            case LOYALTY -> new NeedState(type, 0.5f, 0.02f, 0.01f);
            case CURIOSITY -> new NeedState(type, 0.2f, 0.03f, 0.01f);
        };
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
