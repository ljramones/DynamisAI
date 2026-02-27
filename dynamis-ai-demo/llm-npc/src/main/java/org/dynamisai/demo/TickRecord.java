package org.dynamisai.demo;

import java.util.List;

/**
 * Immutable record of everything that happened in one simulation tick.
 * This record captures the state of the player, guards, crowd, and internal systems.
 *
 * @param tick              The simulation tick number.
 * @param playerAction      The action chosen by the player.
 * @param playerSpeech      The speech entered by the player.
 * @param guard1Dialogue    The dialogue output from guard 1.
 * @param guard2Dialogue    The dialogue output from guard 2.
 * @param guard1Task        The current task/state of guard 1.
 * @param guard2Task        The current task/state of guard 2.
 * @param crowdLod          The current Level of Detail for the crowd.
 * @param socialState       A summary of the current social relationship state.
 * @param memoryEvent       A description of any significant memory events.
 * @param perceptionSummary A summary of what NPCs are perceiving.
 * @param systemsActive     A list of DynamisAI systems that were active during this tick.
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
