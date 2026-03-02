package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommonGoapActionsTest {

    @Test
    void factoriesReturnNonNull() {
        assertNotNull(CommonGoapActions.moveTo());
        assertNotNull(CommonGoapActions.attackThreat());
        assertNotNull(CommonGoapActions.takeCover());
        assertNotNull(CommonGoapActions.requestBackup());
        assertNotNull(CommonGoapActions.flee());
        assertNotNull(CommonGoapActions.waitAction());
    }

    @Test
    void waitActionIsAlwaysApplicable() {
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        assertTrue(CommonGoapActions.waitAction().precondition().test(state));
    }

    @Test
    void attackThreatFailsWithoutThreatEntity() {
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        assertFalse(CommonGoapActions.attackThreat().precondition().test(state));
    }

    @Test
    void requestBackupWritesBackupRequestedFact() {
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        WorldState updated = CommonGoapActions.requestBackup().apply(state);

        assertTrue(updated.has(SquadFacts.BACKUP_REQUESTED));
        assertEquals(true, updated.get(SquadFacts.BACKUP_REQUESTED));
    }
}
