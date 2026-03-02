package org.dynamisai.core;

/**
 * Identifies the origin of a belief held by an agent.
 *
 * <p>ADR-001 invariant: agents may invent beliefs, never percepts.
 * PERCEPT-sourced beliefs are grounded in canonical world state and
 * may only be created via BeliefModel.updateFromPerception().
 */
public enum BeliefSource {

    /**
     * Derived directly from a Percept received via PerceptBus.
     * Cannot be wrong by definition — grounded in canonical world state.
     * Only BeliefModel.updateFromPerception() may produce this source.
     */
    PERCEPT,

    /**
     * Agent's own classification or conclusion derived from percepts.
     * May be wrong.
     */
    INFERRED,

    /**
     * Received from another agent via RumorPropagator.
     * Often wrong — subject to fidelity decay and hop attenuation.
     */
    RUMOR
}
