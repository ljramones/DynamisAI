package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.memory.MemoryStats;

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
