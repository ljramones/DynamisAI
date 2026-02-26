package org.dynamisai.memory;

/**
 * The five lifecycle stages every NPC memory record passes through.
 * Transitions are governed by MemoryLifecycleManager — never set directly.
 */
public enum MemoryLifecycleStage {
    RAW_EVENT,
    SHORT_TERM,
    CONSOLIDATED,
    ARCHIVED,
    PRUNED
}
