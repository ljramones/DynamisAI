package org.dynamisai.cognition;

/**
 * Emotional state vector — drives TTS prosody, facial rig, and dialogue tone.
 * All fields are normalized floats with defined ranges.
 */
public record AffectVector(
    float valence,
    float arousal,
    float dominance,
    float sarcasm,
    float intensity
) {
    public AffectVector {
        validate("valence", valence, -1f, 1f);
        validate("arousal", arousal, 0f, 1f);
        validate("dominance", dominance, 0f, 1f);
        validate("sarcasm", sarcasm, 0f, 1f);
        validate("intensity", intensity, 0f, 1f);
    }

    private static void validate(String name, float v, float min, float max) {
        if (v < min || v > max) {
            throw new IllegalArgumentException(
                name + " must be in [" + min + ", " + max + "] but was " + v);
        }
    }

    public static AffectVector neutral() {
        return new AffectVector(0f, 0.3f, 0.5f, 0f, 0.3f);
    }

    public static AffectVector fearful() {
        return new AffectVector(-0.7f, 0.9f, 0.1f, 0f, 0.8f);
    }

    public static AffectVector angry() {
        return new AffectVector(-0.5f, 0.8f, 0.9f, 0f, 0.9f);
    }

    public static AffectVector content() {
        return new AffectVector(0.6f, 0.3f, 0.6f, 0f, 0.4f);
    }
}
