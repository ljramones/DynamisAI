package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisscripting.api.value.Intent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanToIntentAdapterTest {

    @Test
    void moveToMapsToLocomotionMoveToWithPublicScope() {
        HtnTask.PrimitiveTask task = primitive("MOVE_TO", "Move");
        Intent intent = PlanToIntentAdapter.adapt(
            EntityId.of(1L), task, List.of(EntityId.of(2L)), CanonTime.of(10L, 20L));

        assertEquals("locomotion.moveTo", intent.intentType());
        assertEquals(Intent.RequestedScope.PUBLIC, intent.requestedScope());
    }

    @Test
    void attackMapsToCombatAttackWithPublicScope() {
        HtnTask.PrimitiveTask task = primitive("ATTACK", "Attack");
        Intent intent = PlanToIntentAdapter.adapt(
            EntityId.of(1L), task, List.of(EntityId.of(3L)), CanonTime.of(10L, 20L));

        assertEquals("combat.attack", intent.intentType());
        assertEquals(Intent.RequestedScope.PUBLIC, intent.requestedScope());
    }

    @Test
    void unknownTaskIdFallsBackToAiNamespace() {
        HtnTask.PrimitiveTask task = primitive("DANCE", "Dance");
        Intent intent = PlanToIntentAdapter.adapt(
            EntityId.of(1L), task, List.of(), CanonTime.of(10L, 20L));

        assertEquals("ai.dance", intent.intentType());
        assertEquals(Intent.RequestedScope.PUBLIC, intent.requestedScope());
    }

    @Test
    void nullTaskIdFallsBackToAiUnknown() {
        HtnTask.PrimitiveTask task = primitive(null, "Fallback");
        Intent intent = PlanToIntentAdapter.adapt(
            EntityId.of(1L), task, List.of(), CanonTime.of(10L, 20L));

        assertEquals("ai.unknown", intent.intentType());
    }

    @Test
    void forbiddenAtTier2RecognizesCombatAttack() {
        assertTrue(IntentTypeRegistry.isForbiddenAtTier2("combat.attack"));
    }

    @Test
    void forbiddenAtTier2AllowsLocomotionMoveTo() {
        assertFalse(IntentTypeRegistry.isForbiddenAtTier2("locomotion.moveTo"));
    }

    @Test
    void adaptProducesIntentWithExpectedFields() {
        EntityId agentId = EntityId.of(11L);
        EntityId targetId = EntityId.of(12L);
        CanonTime canonTime = CanonTime.of(77L, 1234L);
        HtnTask.PrimitiveTask task = primitive("MOVE_TO", "Travel to destination");

        Intent intent = PlanToIntentAdapter.adapt(agentId, task, List.of(targetId), canonTime);

        assertEquals(agentId, intent.agentId());
        assertEquals("locomotion.moveTo", intent.intentType());
        assertEquals(List.of(targetId), intent.targets());
        assertEquals("Travel to destination", intent.rationale());
        assertEquals(1.0, intent.confidence());
        assertEquals(canonTime, intent.canonTimeSnapshot());
        assertEquals(Intent.RequestedScope.PUBLIC, intent.requestedScope());
    }

    private static HtnTask.PrimitiveTask primitive(String taskId, String description) {
        return new HtnTask.PrimitiveTask(
            taskId,
            description,
            ignored -> true,
            List.of(),
            1.0f,
            () -> {}
        );
    }
}
