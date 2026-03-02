package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BtConditionTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 7L);
    }

    @Test
    void truePredicateReturnsSuccess() {
        BtCondition c = new BtCondition("t", x -> true);
        assertEquals(BtStatus.SUCCESS, c.tick(ctx()));
    }

    @Test
    void falsePredicateReturnsFailure() {
        BtCondition c = new BtCondition("f", x -> false);
        assertEquals(BtStatus.FAILURE, c.tick(ctx()));
    }

    @Test
    void neverReturnsRunning() {
        BtCondition c = new BtCondition("x", x -> true);
        assertNotEquals(BtStatus.RUNNING, c.tick(ctx()));
    }

    @Test
    void resetNoOp() {
        BtCondition c = new BtCondition("x", x -> true);
        assertDoesNotThrow(c::reset);
    }
}
