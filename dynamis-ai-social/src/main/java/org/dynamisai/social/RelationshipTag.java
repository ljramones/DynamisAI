package org.dynamisai.social;

/**
 * Qualitative tags that can be attached to a Relationship.
 * Multiple tags may coexist — e.g. TRUSTED and INDEBTED simultaneously.
 * Tags complement the numeric trust/affinity scores with discrete semantics
 * the HTN planner and DialogueRequest can pattern-match against.
 */
public enum RelationshipTag {
    ALLY,
    ENEMY,
    NEUTRAL,
    TRUSTED,
    FEARED,
    INDEBTED,
    RIVAL,
    BETRAYED,
    PROTECTED,
    WITNESS
}
