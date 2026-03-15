package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorTreeTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()), null,
            1L, 101L);
    }

    @Test
    void autoResetsAfterSuccess() {
        AtomicInteger count = new AtomicInteger();
        BehaviorTree tree = new BehaviorTree("t",
            new BtAction("a", c -> {
                count.incrementAndGet();
                return BtStatus.SUCCESS;
            }));

        tree.tick(ctx());
        tree.tick(ctx());

        assertEquals(2, count.get());
    }

    @Test
    void autoResetsAfterFailure() {
        AtomicInteger count = new AtomicInteger();
        BehaviorTree tree = new BehaviorTree("t",
            new BtAction("a", c -> {
                count.incrementAndGet();
                return BtStatus.FAILURE;
            }));

        tree.tick(ctx());
        tree.tick(ctx());

        assertEquals(2, count.get());
    }

    @Test
    void runningTreeDoesNotResetBetweenTicks() {
        AtomicInteger count = new AtomicInteger();
        BehaviorTree tree = new BehaviorTree("t",
            new BtAction("a", c -> {
                count.incrementAndGet();
                return count.get() == 1 ? BtStatus.RUNNING : BtStatus.SUCCESS;
            }));

        assertEquals(BtStatus.RUNNING, tree.tick(ctx()));
        assertEquals(BtStatus.SUCCESS, tree.tick(ctx()));
        assertEquals(2, count.get());
    }

    @Test
    void resetForcesRestart() {
        AtomicInteger count = new AtomicInteger();
        BehaviorTree tree = new BehaviorTree("t",
            new BtAction("a", c -> {
                count.incrementAndGet();
                return BtStatus.SUCCESS;
            }));

        tree.tick(ctx());
        tree.reset();
        tree.tick(ctx());

        assertEquals(2, count.get());
    }
}
