package org.dynamisai.memory;

/**
 * Per-NPC memory capacity configuration.
 * When any stage exceeds its limit, MemoryLifecycleManager prunes
 * lowest-importance records first.
 */
public record MemoryBudget(
    int maxRawEvents,
    int maxShortTerm,
    int maxConsolidated,
    int maxArchived
) {
    public MemoryBudget {
        if (maxRawEvents <= 0) throw new IllegalArgumentException("maxRawEvents must be > 0");
        if (maxShortTerm <= 0) throw new IllegalArgumentException("maxShortTerm must be > 0");
        if (maxConsolidated <= 0) throw new IllegalArgumentException("maxConsolidated must be > 0");
        if (maxArchived <= 0) throw new IllegalArgumentException("maxArchived must be > 0");
    }

    /** Default budget for a Tier 0 (cinematic/major) NPC. */
    public static MemoryBudget tier0() {
        return new MemoryBudget(100, 50, 25, 200);
    }

    /** Default budget for a Tier 1 (nearby) NPC. */
    public static MemoryBudget tier1() {
        return new MemoryBudget(50, 25, 10, 50);
    }

    /** Minimal budget for a background NPC. */
    public static MemoryBudget background() {
        return new MemoryBudget(10, 5, 3, 10);
    }
}
