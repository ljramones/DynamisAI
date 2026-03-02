package org.dynamisai.core;

import org.dynamis.core.entity.EntityId;
import java.util.List;

/**
 * Animation update for a single entity.
 */
public record AnimationSignal(
    EntityId entityId,
    String affectSummary,
    float valence,
    float arousal,
    boolean isSpeaking,
    List<String> visemeSequence
) {
    public static AnimationSignal idle(EntityId id) {
        return new AnimationSignal(id, "neutral", 0f, 0f, false, List.of());
    }
}
