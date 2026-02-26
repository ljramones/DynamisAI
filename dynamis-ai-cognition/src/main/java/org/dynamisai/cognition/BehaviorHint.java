package org.dynamisai.cognition;

/**
 * A planner hint from the LLM — a suggestion, never a command.
 * The HTN/GOAP planner decides whether to act on it.
 */
public record BehaviorHint(
    String hintType,
    float confidence,
    String context
) {
    public BehaviorHint {
        if (confidence < 0f || confidence > 1f) {
            throw new IllegalArgumentException("confidence must be in [0,1]");
        }
    }
}
