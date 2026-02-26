package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.navigation.NavigationSystem;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Standard NPC task library — compound tasks built from primitive tasks.
 * All tasks here are data — no game engine references.
 */
public final class TaskLibrary {

    private TaskLibrary() {}

    public static HtnTask.PrimitiveTask idleTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "idle", "Stand idle",
            state -> true,
            List.of(new PlannerEffect("isIdle", true)),
            0.1f, operator
        );
    }

    public static HtnTask.PrimitiveTask fleeThreatTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "flee-threat", "Flee from threat",
            state -> state.currentThreat().ordinal() >= ThreatLevel.HIGH.ordinal(),
            List.of(
                new PlannerEffect("isFleeing", true),
                new PlannerEffect("isIdle", false)
            ),
            1.0f, operator
        );
    }

    public static HtnTask.PrimitiveTask seekCoverTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "seek-cover", "Move to nearest cover",
            state -> state.currentThreat().ordinal() >= ThreatLevel.MEDIUM.ordinal(),
            List.of(new PlannerEffect("hasCover", true)),
            0.8f, operator
        );
    }

    public static HtnTask.PrimitiveTask approachPlayerTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "approach-player", "Move toward player",
            state -> state.currentThreat() == ThreatLevel.NONE
                && state.perception().percepts().size() > 0,
            List.of(new PlannerEffect("nearPlayer", true)),
            0.5f, operator
        );
    }

    public static HtnTask.PrimitiveTask initiateDialogueTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "initiate-dialogue", "Start dialogue with player",
            state -> state.is("nearPlayer", true)
                && state.currentThreat() == ThreatLevel.NONE,
            List.of(new PlannerEffect("inDialogue", true)),
            0.3f, operator
        );
    }

    public static HtnTask.PrimitiveTask callBackupTask(Runnable operator) {
        return new HtnTask.PrimitiveTask(
            "call-backup", "Signal for backup",
            state -> state.currentThreat().ordinal() >= ThreatLevel.MEDIUM.ordinal()
                && !state.is("backupCalled", true),
            List.of(new PlannerEffect("backupCalled", true)),
            0.6f, operator
        );
    }

    /** fleeThreatTask wired to NavigationSystem. */
    public static HtnTask.PrimitiveTask fleeThreatTask(NavigationSystem nav,
                                                       EntityId agent,
                                                       Location agentPos,
                                                       Location threatPos) {
        return fleeThreatTask(NavigationOperators.fleeFrom(nav, agent, agentPos, threatPos));
    }

    /** approachPlayerTask wired to NavigationSystem. */
    public static HtnTask.PrimitiveTask approachPlayerTask(NavigationSystem nav,
                                                           EntityId agent,
                                                           Location goal) {
        return approachPlayerTask(NavigationOperators.moveTo(nav, agent, goal));
    }

    /** seekCoverTask wired to NavigationSystem. */
    public static HtnTask.PrimitiveTask seekCoverTask(NavigationSystem nav,
                                                      EntityId agent,
                                                      Location coverPos) {
        return seekCoverTask(NavigationOperators.moveTo(nav, agent, coverPos));
    }

    /** idleTask wired to NavigationSystem stop. */
    public static HtnTask.PrimitiveTask idleTask(NavigationSystem nav,
                                                 EntityId agent) {
        return idleTask(NavigationOperators.stop(nav, agent));
    }

    public static HtnTask.CompoundTask surviveTask(AtomicInteger fleeCount,
                                                   AtomicInteger idleCount,
                                                   AtomicInteger dialogueCount) {
        DecompositionMethod threatResponse = new DecompositionMethod(
            "method-threat-response",
            state -> state.currentThreat().ordinal() >= ThreatLevel.HIGH.ordinal(),
            List.of(
                fleeThreatTask(fleeCount::incrementAndGet),
                callBackupTask(() -> {})
            )
        );

        DecompositionMethod socialApproach = new DecompositionMethod(
            "method-social-approach",
            state -> state.currentThreat() == ThreatLevel.NONE
                && state.perception().percepts().size() > 0,
            List.of(
                approachPlayerTask(() -> {}),
                initiateDialogueTask(dialogueCount::incrementAndGet)
            )
        );

        DecompositionMethod fallbackIdle = new DecompositionMethod(
            "method-idle-fallback",
            state -> true,
            List.of(idleTask(idleCount::incrementAndGet))
        );

        return new HtnTask.CompoundTask(
            "survive", "Top-level survival task",
            List.of(threatResponse, socialApproach, fallbackIdle)
        );
    }
}
