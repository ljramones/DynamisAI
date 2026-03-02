package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;

import java.util.List;

/**
 * Converts a selected HtnTask.PrimitiveTask into a scripting-api Intent.
 *
 * <p>Targets: PrimitiveTask has no target field. Targets must be supplied
 * by the caller (extracted from planning context). Pass List.of() when
 * no specific target is required.
 *
 * <p>Confidence: GOAP/HTN-derived intents carry full confidence (1.0) by
 * default - the planner already selected this action as optimal.
 */
public final class PlanToIntentAdapter {

    private PlanToIntentAdapter() {}

    /**
     * Adapts a primitive task to an Intent.
     *
     * @param agentId the acting agent
     * @param task the selected primitive task
     * @param targets explicit target entities (may be empty)
     * @param canonTime current CanonTime snapshot at plan selection
     * @return scripting-api Intent ready for IntentBus emission
     */
    public static Intent adapt(
        EntityId agentId,
        HtnTask.PrimitiveTask task,
        List<EntityId> targets,
        CanonTime canonTime) {

        String intentType = IntentTypeRegistry.resolve(task.taskId());
        Intent.RequestedScope scope = resolveScope(intentType);

        return Intent.of(
            agentId,
            intentType,
            targets,
            task.description(),
            1.0,
            canonTime,
            scope);
    }

    /**
     * Stealth-tagged intent types request STEALTH scope.
     * All others default to PUBLIC.
     */
    private static Intent.RequestedScope resolveScope(String intentType) {
        if (intentType.contains("stealth") || intentType.contains("covert")) {
            return Intent.RequestedScope.STEALTH;
        }
        return Intent.RequestedScope.PUBLIC;
    }
}
