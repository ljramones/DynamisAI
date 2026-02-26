package org.dynamisai.demo;

import org.dynamisai.crowd.CrowdSnapshot;

import java.nio.file.Path;

/**
 * DynamisAI interactive demo - Guard Patrol Encounter.
 */
public final class DynamisAiDemo {

    private static final int MAX_TICKS = 20;

    public static void main(String[] args) throws Exception {
        DemoCli cli = new DemoCli();
        DemoWorld world = new DemoWorld();

        cli.clearScreen();
        cli.println("""
            +========================================================+
            |          DynamisAI - Guard Patrol Encounter Demo       |
            |                                                        |
            |  Two city guards patrol a market square. You enter.   |
            |  Every system in DynamisAI is live - watch labels.    |
            |                                                        |
            |  Press Enter to begin...                               |
            +========================================================+
            """);
        System.in.read();

        for (long tick = 1; tick <= MAX_TICKS; tick++) {
            cli.clearScreen();

            CrowdSnapshot snap = world.crowd.latestSnapshot();
            String lod = snap.groups().containsKey(world.patrolGroup)
                ? snap.groups().get(world.patrolGroup).lod().name()
                : "UNKNOWN";

            float dist = world.guard1.distanceTo(world.player.position);
            String percep = dist < 12f
                ? String.format("Guard1 sees Player (dist=%.1fm)", dist)
                : "Player out of perception range";

            cli.renderTick(
                tick,
                world.guard1,
                world.guard2,
                world.player,
                lod,
                "LINE",
                percep,
                world.guard1.currentTask,
                world.guard2.currentTask,
                world.guard1.lastDialogue,
                world.guard2.lastDialogue,
                world.social.graph().get(world.guard1.id, world.player.id).toString(),
                "see tick log"
            );

            PlayerAction action = cli.promptAction();
            String speech = "";
            if (action == PlayerAction.SPEAK) {
                speech = cli.promptSpeech();
            }

            TickRecord record = world.tick(tick, action, speech);
            world.guard1.lastDialogue = record.guard1Dialogue();

            if (!record.guard1Dialogue().isEmpty()) {
                cli.printEvent(world.guard1.name + " says: \"" + record.guard1Dialogue() + "\"");
            }
            if (action == PlayerAction.HOSTILE) {
                cli.printEvent("You made a hostile act! Guards tags now: "
                    + world.social.graph().get(world.guard1.id, world.player.id).tags());
            }

            if (world.isResolved()) {
                cli.clearScreen();
                cli.printOutcome(world.outcome());
                break;
            }

            Thread.sleep(300);
        }

        if (!world.isResolved()) {
            world.report().setOutcome("Patrol continued - 20 ticks elapsed");
        } else {
            world.report().setOutcome(world.outcome());
        }

        Path reportPath = Path.of("demo-report.json");
        world.report().writeJson(reportPath);
        cli.println("\nDemo complete. Report written to: " + reportPath.toAbsolutePath());
        cli.println("Systems exercised: WorldStateStore, BudgetGovernor, PerceptionSystem, "
            + "CognitionService, SocialDialogueShaper, HtnPlanner, NavigationSystem, "
            + "CrowdSystem, MemoryLifecycleManager, TTSPipeline, AnimisBridge");

        world.shutdown();
    }
}
