package org.dynamisengine.ai.social;

public record BehaviorModifierEvent(
    BehaviorModifierType type,
    float magnitude,
    String targetFactionId,
    String parameter,
    long tick
) {
    public BehaviorModifierEvent {
        if (magnitude < 0f || magnitude > 1f) {
            throw new IllegalArgumentException("magnitude must be [0,1]");
        }
    }
}
