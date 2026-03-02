package org.dynamisai.planning;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps GOAP/HTN taskId strings to scripting-api intentType strings.
 *
 * <p>Convention: taskId uses SCREAMING_SNAKE_CASE (e.g. "MOVE_TO").
 * intentType uses dot-namespaced lowercase (e.g. "locomotion.moveTo").
 *
 * <p>Unknown taskIds map to "ai.<taskId.toLowerCase()>" as a safe default.
 */
public final class IntentTypeRegistry {

    private static final Map<String, String> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("MOVE_TO", "locomotion.moveTo");
        REGISTRY.put("FLEE", "locomotion.flee");
        REGISTRY.put("ATTACK", "combat.attack");
        REGISTRY.put("CONVERSE", "dialogue.initiate");
        REGISTRY.put("GIVE", "economy.transfer");
        REGISTRY.put("PATROL", "locomotion.patrol");
        REGISTRY.put("IDLE", "behaviour.idle");
        REGISTRY.put("INVESTIGATE", "cognition.investigate");
        REGISTRY.put("PICK_UP", "interaction.pickUp");
        REGISTRY.put("DROP", "interaction.drop");
    }

    private IntentTypeRegistry() {}

    /**
     * Resolves a taskId to its canonical intentType string.
     * Falls back to "ai.<taskId_lowercased>" for unknown taskIds.
     */
    public static String resolve(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            return "ai.unknown";
        }
        return REGISTRY.getOrDefault(taskId, "ai." + taskId.toLowerCase().replace('_', '.'));
    }

    /**
     * Returns true if the resolved intentType is forbidden at degradation Tier >= 2.
     */
    public static boolean isForbiddenAtTier2(String intentType) {
        return switch (intentType) {
            case "combat.attack",
                    "combat.murder",
                    "authority.arrest",
                    "evidence.destroy",
                    "social.accuse" -> true;
            default -> false;
        };
    }
}
