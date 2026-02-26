package org.dynamisai.core;

/**
 * Dialogue output event for one NPC utterance.
 */
public record DialogueEvent(
    EntityId speaker,
    EntityId target,
    String text,
    String affectSummary,
    boolean audioReady
) {
    public static DialogueEvent of(EntityId speaker, EntityId target, String text, String affectSummary) {
        return new DialogueEvent(speaker, target, text, affectSummary, false);
    }

    public DialogueEvent withAudioReady() {
        return new DialogueEvent(speaker, target, text, affectSummary, true);
    }
}
