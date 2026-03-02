package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BtSelectorTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 99L);
    }

    @Test
    void firstSuccessShortCircuits() {
        java.util.concurrent.atomic.AtomicInteger successTicks = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger afterTicks = new java.util.concurrent.atomic.AtomicInteger();
        BtAction success = new BtAction("ok", c -> {
            successTicks.incrementAndGet();
            return BtStatus.SUCCESS;
        });
        BtAction after = new BtAction("after", c -> {
            afterTicks.incrementAndGet();
            return BtStatus.FAILURE;
        });
        BtSelector selector = new BtSelector(success, after);

        assertEquals(BtStatus.SUCCESS, selector.tick(ctx()));
        assertEquals(1, successTicks.get());
        assertEquals(0, afterTicks.get());
    }

    @Test
    void allFailureReturnsFailure() {
        BtSelector selector = new BtSelector(
            new BtAction("a", c -> BtStatus.FAILURE),
            new BtAction("b", c -> BtStatus.FAILURE)
        );
        assertEquals(BtStatus.FAILURE, selector.tick(ctx()));
    }

    @Test
    void failureThenRunningReturnsRunning() {
        BtSelector selector = new BtSelector(
            new BtAction("a", c -> BtStatus.FAILURE),
            new BtAction("b", c -> BtStatus.RUNNING)
        );
        assertEquals(BtStatus.RUNNING, selector.tick(ctx()));
    }

    @Test
    void resetRestartsFromFirstChild() {
        java.util.concurrent.atomic.AtomicInteger firstTicks = new java.util.concurrent.atomic.AtomicInteger();
        BtAction first = new BtAction("first", c -> {
            firstTicks.incrementAndGet();
            return BtStatus.FAILURE;
        });
        BtSelector selector = new BtSelector(first, new BtAction("b", c -> BtStatus.SUCCESS));
        selector.tick(ctx());
        selector.reset();
        selector.tick(ctx());

        assertEquals(2, firstTicks.get());
    }
}
