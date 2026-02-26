package org.dynamisai.demo;

/**
 * Actions available to the human player each tick.
 */
public enum PlayerAction {
    /** The player approaches the guards. */
    APPROACH,
    /** The player performs a hostile act. */
    HOSTILE,
    /** The player flees from the guards. */
    FLEE,
    /** The player waits in place. */
    WAIT,
    /** The player speaks to the guards. */
    SPEAK;

    /**
     * Parses a string input into a PlayerAction.
     * Supported inputs are the first letter of each action (A, H, F, W, S).
     * Defaults to {@link #WAIT} for unknown inputs.
     *
     * @param input The raw input string.
     * @return The corresponding PlayerAction.
     */
    public static PlayerAction parse(String input) {
        return switch (input.trim().toUpperCase()) {
            case "A" -> APPROACH;
            case "H" -> HOSTILE;
            case "F" -> FLEE;
            case "W" -> WAIT;
            case "S" -> SPEAK;
            default -> WAIT;
        };
    }
}
