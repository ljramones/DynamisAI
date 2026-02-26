package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;

/**
 * Maps AffectVector dimensions to voice synthesis style parameters.
 */
public final class AffectToVoiceStyle {

    private AffectToVoiceStyle() {
    }

    public record VoiceStyle(
        String kokokoToken,
        float pitchMult,
        float rateMult
    ) {
        public static VoiceStyle neutral() {
            return new VoiceStyle("[neutral]", 1.0f, 1.0f);
        }
    }

    public static VoiceStyle from(AffectVector affect) {
        if (affect == null) {
            return VoiceStyle.neutral();
        }

        float v = affect.valence();
        float a = affect.arousal();
        float d = affect.dominance();

        String token;
        if (v > 0.4f) {
            token = "[happy]";
        } else if (v < -0.5f && a > 0.5f) {
            token = "[angry]";
        } else if (v < -0.4f && a < 0.3f) {
            token = "[sad]";
        } else if (a > 0.7f && d < 0.3f) {
            token = "[fearful]";
        } else if (a > 0.6f && v > 0f) {
            token = "[surprised]";
        } else {
            token = "[neutral]";
        }

        float pitch = clamp(1.0f - ((d - 0.5f) * 0.30f), 0.85f, 1.15f);
        float rate = clamp(1.0f + (a * 0.30f), 0.85f, 1.30f);

        return new VoiceStyle(token, pitch, rate);
    }

    public static String applyToText(String text, AffectVector affect) {
        VoiceStyle style = from(affect);
        return style.kokokoToken() + " " + text;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
