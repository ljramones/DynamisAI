package org.dynamisengine.ai.tools;

import java.util.List;

public record DecisionTraceEntry(
    String goal,
    String planName,
    String currentAction,
    float confidence,
    List<String> memoryEvidence
) {
    public DecisionTraceEntry {
        memoryEvidence = memoryEvidence == null ? List.of() : List.copyOf(memoryEvidence);
    }
}
