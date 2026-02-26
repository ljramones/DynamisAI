package org.dynamisai.core;

import java.util.List;
import java.util.Map;

public record WorldFacts(
    Map<String, Object> facts,
    List<EntityId> nearbyActors,
    ThreatLevel currentThreat,
    Location agentPosition,
    NarrativeRails rails
) {}
