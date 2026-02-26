package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommonBehaviorTreesTest {

    private BtContext ctx() {
        return new BtContext(
            EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            new SquadBlackboard("alpha"),
            1L,
            123L
        );
    }

    @Test
    void factoriesReturnNonNull() {
        assertNotNull(CommonBehaviorTrees.guardPatrol());
        assertNotNull(CommonBehaviorTrees.combatEngagement());
        assertNotNull(CommonBehaviorTrees.flee());
    }

    @Test
    void treesTickWithoutException() {
        assertDoesNotThrow(() -> CommonBehaviorTrees.guardPatrol().tick(ctx()));
        assertDoesNotThrow(() -> CommonBehaviorTrees.combatEngagement().tick(ctx()));
        assertDoesNotThrow(() -> CommonBehaviorTrees.flee().tick(ctx()));
    }
}
