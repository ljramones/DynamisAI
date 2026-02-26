package org.dynamisai.tools;

import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;

/**
 * Result of one simulation tick in the demo reel.
 * Used for assertions and JSON report generation.
 */
public record DemoTickResult(
    long tick,
    int perceptCount,
    ThreatLevel aggregateThreat,
    String dialogueText,
    boolean dialogueFromCache,
    MemoryStats memoryStats,
    long frameBudgetMs
) {}
