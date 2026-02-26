package org.dynamisai.demo;

import java.util.Scanner;

/**
 * Console I/O for the DynamisAI Guard Patrol demo.
 * Handles rendering the world state to the console and prompting for player input.
 */
public final class DemoCli {

    private static final String DIVIDER =
        "----------------------------------------------------------";
    private static final String HEADER =
        "==========================================================";

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Clears the console screen using ANSI escape codes.
     */
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Renders a single simulation tick to the console.
     * Displays positions, threat levels, mood, crowd status, perception, plans, speech, social, and memory lines.
     *
     * @param tick           The current simulation tick number.
     * @param guard1         The first guard NPC.
     * @param guard2         The second guard NPC.
     * @param player         The player NPC.
     * @param crowdLod       The current Level of Detail for the crowd.
     * @param formationName  The name of the current crowd formation.
     * @param perceptionLine A summary of what the NPCs perceive.
     * @param guard1Plan     The current plan for guard 1.
     * @param guard2Plan     The current plan for guard 2.
     * @param guard1Speech   The current speech output for guard 1.
     * @param guard2Speech   The current speech output for guard 2.
     * @param socialLine     A summary of social interactions.
     * @param memoryLine     A summary of relevant memories.
     */
    public void renderTick(long tick,
                           DemoNpc guard1, DemoNpc guard2, DemoNpc player,
                           String crowdLod, String formationName,
                           String perceptionLine,
                           String guard1Plan, String guard2Plan,
                           String guard1Speech, String guard2Speech,
                           String socialLine,
                           String memoryLine) {

        System.out.println(HEADER);
        System.out.printf("  TICK %-3d  |  DynamisAI Demo - Guard Patrol Encounter%n", tick);
        System.out.println(HEADER);

        System.out.printf("WORLD  | %-12s pos:(%.0f,%.0f,%.0f) threat:%-8s mood:%.2fv%n",
            guard1.name,
            guard1.position.x(), guard1.position.y(), guard1.position.z(),
            guard1.perceivedThreat,
            guard1.affect.valence());

        System.out.printf("       | %-12s pos:(%.0f,%.0f,%.0f) threat:%-8s mood:%.2fv%n",
            guard2.name,
            guard2.position.x(), guard2.position.y(), guard2.position.z(),
            guard2.perceivedThreat,
            guard2.affect.valence());

        System.out.printf("       | %-12s pos:(%.0f,%.0f,%.0f)%n",
            "Player",
            player.position.x(), player.position.y(), player.position.z());

        System.out.println(DIVIDER);

        System.out.printf("CROWD  | %s formation, LOD:%s%n", formationName, crowdLod);
        System.out.printf("PERCEP | %s%n", perceptionLine);
        System.out.printf("PLAN   | %s -> %s%n", guard1.name, guard1Plan);
        System.out.printf("       | %s -> %s%n", guard2.name, guard2Plan);

        if (!guard1Speech.isEmpty()) {
            System.out.printf("SPEECH | %s: \"%s\"%n", guard1.name, guard1Speech);
        }
        if (!guard2Speech.isEmpty()) {
            System.out.printf("       | %s: \"%s\"%n", guard2.name, guard2Speech);
        }

        System.out.printf("SOCIAL | %s%n", socialLine);
        System.out.printf("MEMORY | %s%n", memoryLine);

        System.out.println(HEADER);
    }

    /**
     * Prompts the player to select an action from the available options.
     *
     * @return The {@link PlayerAction} chosen by the player.
     */
    public PlayerAction promptAction() {
        System.out.println("  Your action:  [A]pproach   [H]ostile act   [F]lee   [W]ait   [S]peak");
        System.out.print("  > ");
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "W";
        return PlayerAction.parse(line.isEmpty() ? "W" : line.substring(0, 1));
    }

    /**
     * Prompts the player to enter speech text.
     *
     * @return The speech string entered by the player, or an empty string if skipped.
     */
    public String promptSpeech() {
        System.out.print("  Your speech (Enter to skip): ");
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    /**
     * Prints a highlighted event message to the console.
     *
     * @param event The event description to print.
     */
    public void printEvent(String event) {
        System.out.println("\033[33m  * " + event + "\033[0m");
    }

    /**
     * Prints the final outcome of the demo.
     *
     * @param outcome The outcome description.
     */
    public void printOutcome(String outcome) {
        System.out.println(HEADER);
        System.out.println("  OUTCOME: " + outcome);
        System.out.println(HEADER);
    }

    /**
     * Prints a line of text to the console.
     *
     * @param s The string to print.
     */
    public void println(String s) {
        System.out.println(s);
    }
}
