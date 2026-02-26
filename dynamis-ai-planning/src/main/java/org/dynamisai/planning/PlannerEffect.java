package org.dynamisai.planning;

/**
 * A single blackboard mutation produced by a primitive task.
 * Effects are applied to produce the next WorldState during plan simulation.
 */
public record PlannerEffect(String key, Object value) {}
