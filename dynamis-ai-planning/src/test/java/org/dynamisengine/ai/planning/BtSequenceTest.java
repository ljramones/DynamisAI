package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BtSequenceTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 99L);
    }

    @Test
    void allChildrenSuccessReturnsSuccess() {
        BtSequence seq = new BtSequence(
            new BtAction("a", c -> BtStatus.SUCCESS),
            new BtAction("b", c -> BtStatus.SUCCESS)
        );
        assertEquals(BtStatus.SUCCESS, seq.tick(ctx()));
    }

    @Test
    void firstFailureStopsSequence() {
        AtomicInteger failTicks = new AtomicInteger();
        AtomicInteger afterTicks = new AtomicInteger();
        BtAction fail = new BtAction("fail", c -> {
            failTicks.incrementAndGet();
            return BtStatus.FAILURE;
        });
        BtAction after = new BtAction("after", c -> {
            afterTicks.incrementAndGet();
            return BtStatus.SUCCESS;
        });
        BtSequence seq = new BtSequence(fail, after);

        assertEquals(BtStatus.FAILURE, seq.tick(ctx()));
        assertEquals(1, failTicks.get());
        assertEquals(0, afterTicks.get());
    }

    @Test
    void firstRunningStopsThisFrame() {
        AtomicInteger runningTicks = new AtomicInteger();
        AtomicInteger afterTicks = new AtomicInteger();
        BtAction running = new BtAction("run", c -> {
            runningTicks.incrementAndGet();
            return BtStatus.RUNNING;
        });
        BtAction after = new BtAction("after", c -> {
            afterTicks.incrementAndGet();
            return BtStatus.SUCCESS;
        });
        BtSequence seq = new BtSequence(running, after);

        assertEquals(BtStatus.RUNNING, seq.tick(ctx()));
        assertEquals(1, runningTicks.get());
        assertEquals(0, afterTicks.get());
    }

    @Test
    void resumesFromRunningChild() {
        AtomicInteger n = new AtomicInteger();
        BtAction first = new BtAction("first", c -> {
            if (n.getAndIncrement() == 0) {
                return BtStatus.RUNNING;
            }
            return BtStatus.SUCCESS;
        });
        AtomicInteger secondTicks = new AtomicInteger();
        BtAction second = new BtAction("second", c -> {
            secondTicks.incrementAndGet();
            return BtStatus.SUCCESS;
        });
        BtSequence seq = new BtSequence(first, second);

        assertEquals(BtStatus.RUNNING, seq.tick(ctx()));
        assertEquals(BtStatus.SUCCESS, seq.tick(ctx()));
        assertEquals(1, secondTicks.get());
    }

    @Test
    void resetRestartsFromFirstChild() {
        AtomicInteger firstTicks = new AtomicInteger();
        BtAction first = new BtAction("first", c -> {
            firstTicks.incrementAndGet();
            return BtStatus.SUCCESS;
        });
        BtSequence seq = new BtSequence(first);
        seq.tick(ctx());
        seq.reset();
        seq.tick(ctx());

        assertEquals(2, firstTicks.get());
    }
}
