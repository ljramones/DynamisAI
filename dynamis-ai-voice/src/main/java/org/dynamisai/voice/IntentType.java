package org.dynamisai.voice;

/**
 * Predictive intent signals sent to Animis 180ms before an action commits.
 * Animis uses these to begin motion-matching blend before the action fires.
 */
public enum IntentType {
    ATTACK_LEFT,
    ATTACK_RIGHT,
    ATTACK_OVERHEAD,
    BLOCK,
    DODGE_LEFT,
    DODGE_RIGHT,
    SPRINT,
    CROUCH,
    STAND,
    JUMP,
    CLIMB,
    SURRENDER,
    FLEE,
    APPROACH_FRIENDLY,
    APPROACH_HOSTILE,
    IDLE,
    COWER,
    THREATEN,
    GRIEVE,
    CELEBRATE
}
