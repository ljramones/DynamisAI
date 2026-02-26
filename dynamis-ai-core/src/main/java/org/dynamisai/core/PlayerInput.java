package org.dynamisai.core;

/**
 * Player action payload submitted by a game engine each tick.
 */
public record PlayerInput(
    EntityId playerId,
    String actionType,
    EntityId targetId,
    String speechText,
    float intensity
) {
    public static PlayerInput of(EntityId playerId, String actionType) {
        return new PlayerInput(playerId, actionType, null, null, 1.0f);
    }

    public static PlayerInput speech(EntityId playerId, EntityId target, String text) {
        return new PlayerInput(playerId, "SPEAK", target, text, 0.5f);
    }

    public boolean hasSpeech() {
        return speechText != null && !speechText.isBlank();
    }

    public boolean hasTarget() {
        return targetId != null;
    }
}
