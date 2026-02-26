package org.dynamisai.voice;

/**
 * Predictive intent signal — submitted to Animis before the action commits.
 * Animis begins motion-matching blend anticipateBy milliseconds early.
 */
public record IntentSignal(
    IntentType type,
    float confidence,
    Direction direction
) {
    public IntentSignal {
        if (confidence < 0f || confidence > 1f) {
            throw new IllegalArgumentException("confidence must be in [0,1]");
        }
    }

    /** High-confidence signal with no directional hint. */
    public static IntentSignal certain(IntentType type) {
        return new IntentSignal(type, 1.0f, Direction.none());
    }

    /** Signal with direction and confidence. */
    public static IntentSignal of(IntentType type, float confidence, Direction direction) {
        return new IntentSignal(type, confidence, direction);
    }
}
