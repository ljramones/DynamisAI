package org.dynamisai.cognition;

import java.util.List;

/**
 * Schema-constrained LLM output. Raw text never flows into game logic —
 * only this typed record does.
 */
public record DialogueResponse(
    String text,
    AffectVector affect,
    List<String> nonverbalTags,
    List<BehaviorHint> hints,
    boolean fromCache
) {
    /** Safe fallback — always available, never null. */
    public static DialogueResponse fallback(String text) {
        return new DialogueResponse(
            text, AffectVector.neutral(),
            List.of(), List.of(), false
        );
    }

    /** Cached variant of a response — marks it as served from cache. */
    public DialogueResponse asCached() {
        return new DialogueResponse(text, affect, nonverbalTags, hints, true);
    }
}
