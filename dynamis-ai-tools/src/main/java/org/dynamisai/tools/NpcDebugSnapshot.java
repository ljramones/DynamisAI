package org.dynamisai.tools;

import org.dynamis.core.entity.EntityId;

import java.util.List;
import java.util.Map;

public record NpcDebugSnapshot(
    EntityId agent,
    long tick,
    AffectRadarSnapshot affectRadar,
    DecisionTraceEntry decisionTrace,
    List<PerceptionOverlayEntry> perceptionOverlay,
    String activeHtnGoal,
    Map<String, Float> beliefSummary,
    List<String> activeFlags
) {
    public NpcDebugSnapshot {
        perceptionOverlay = perceptionOverlay == null ? List.of() : List.copyOf(perceptionOverlay);
        beliefSummary = beliefSummary == null ? Map.of() : Map.copyOf(beliefSummary);
        activeFlags = activeFlags == null ? List.of() : List.copyOf(activeFlags);
    }
}
