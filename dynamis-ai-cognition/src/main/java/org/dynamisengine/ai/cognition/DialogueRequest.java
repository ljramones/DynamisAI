package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.WorldFacts;
import org.dynamisengine.ai.core.WorldSnapshot;

public record DialogueRequest(
    EntityId speaker,
    EntityId target,
    String inputSpeech,
    WorldFacts context,
    AffectVector currentMood,
    WorldSnapshot snapshot
) {}
