package org.dynamisai.demo;

import java.util.List;

/**
 * Immutable record of everything that happened in one tick.
 */
public record TickRecord(
    long tick,
    String playerAction,
    String playerSpeech,
    String guard1Dialogue,
    String guard2Dialogue,
    String guard1Task,
    String guard2Task,
    String crowdLod,
    String socialState,
    String memoryEvent,
    String perceptionSummary,
    List<String> systemsActive
) {}
