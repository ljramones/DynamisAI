package org.dynamisai.core;

import java.util.Set;

public record NarrativeRails(
    String questStage,
    Set<String> forbiddenTopics,
    Set<String> canonConstraints,
    String toneLock
) {}
