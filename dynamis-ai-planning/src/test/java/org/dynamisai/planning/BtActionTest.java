package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BtActionTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 3L);
    }

    @Test
    void returnsSuccess() {
        BtAction a = new BtAction("x", c -> BtStatus.SUCCESS);
        assertEquals(BtStatus.SUCCESS, a.tick(ctx()));
    }

    @Test
    void returnsRunning() {
        BtAction a = new BtAction("x", c -> BtStatus.RUNNING);
        assertEquals(BtStatus.RUNNING, a.tick(ctx()));
        assertTrue(a.isRunning());
    }

    @Test
    void resetClearsRunningState() {
        AtomicInteger calls = new AtomicInteger();
        BtAction a = new BtAction("x", c -> calls.getAndIncrement() == 0 ? BtStatus.RUNNING : BtStatus.SUCCESS);
        a.tick(ctx());
        assertTrue(a.isRunning());
        a.reset();
        assertFalse(a.isRunning());
    }
}
