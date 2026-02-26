package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.time.Instant;

/**
 * One turn of dialogue between two entities.
 * Stored in DialogueHistory — bounded ring per entity pair.
 */
public record DialogueEntry(
    EntityId speaker,
    EntityId listener,
    String text,
    String topic,
    float sentiment,
    Instant timestamp,
    long tick
) {
    public static DialogueEntry of(EntityId speaker, EntityId listener,
                                    String text, String topic,
                                    float sentiment, long tick) {
        return new DialogueEntry(speaker, listener, text, topic,
            sentiment, Instant.now(), tick);
    }

    public boolean isPositive() { return sentiment >  0.1f; }
    public boolean isNegative() { return sentiment < -0.1f; }
}
