package org.dynamisai.demo;

/**
 * Actions available to the human player each tick.
 */
public enum PlayerAction {
    APPROACH,
    HOSTILE,
    FLEE,
    WAIT,
    SPEAK;

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
