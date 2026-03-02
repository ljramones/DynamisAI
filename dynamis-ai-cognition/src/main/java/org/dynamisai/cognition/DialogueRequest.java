package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.core.WorldSnapshot;

public record DialogueRequest(
    EntityId speaker,
    EntityId target,
    String inputSpeech,
    WorldFacts context,
    AffectVector currentMood,
    WorldSnapshot snapshot
) {}
