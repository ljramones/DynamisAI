package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BtParallelTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 11L);
    }

    @Test
    void requireAllSuccessNeedsAllSuccess() {
        BtParallel p = new BtParallel(BtParallel.Policy.REQUIRE_ALL,
            BtParallel.Policy.REQUIRE_ONE,
            List.of(new BtAction("a", c -> BtStatus.SUCCESS),
                new BtAction("b", c -> BtStatus.SUCCESS)));
        assertEquals(BtStatus.SUCCESS, p.tick(ctx()));
    }

    @Test
    void requireAllSuccessFailsIfOneFails() {
        BtParallel p = new BtParallel(BtParallel.Policy.REQUIRE_ALL,
            BtParallel.Policy.REQUIRE_ONE,
            List.of(new BtAction("a", c -> BtStatus.SUCCESS),
                new BtAction("b", c -> BtStatus.FAILURE)));
        assertEquals(BtStatus.FAILURE, p.tick(ctx()));
    }

    @Test
    void requireOneSuccessSucceedsOnAnySuccess() {
        BtParallel p = new BtParallel(BtParallel.Policy.REQUIRE_ONE,
            BtParallel.Policy.REQUIRE_ALL,
            List.of(new BtAction("a", c -> BtStatus.FAILURE),
                new BtAction("b", c -> BtStatus.SUCCESS)));
        assertEquals(BtStatus.SUCCESS, p.tick(ctx()));
    }

    @Test
    void allChildrenTickedEveryFrame() {
        java.util.concurrent.atomic.AtomicInteger oneTicks = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger twoTicks = new java.util.concurrent.atomic.AtomicInteger();
        BtAction one = new BtAction("one", c -> {
            oneTicks.incrementAndGet();
            return BtStatus.RUNNING;
        });
        BtAction two = new BtAction("two", c -> {
            twoTicks.incrementAndGet();
            return BtStatus.RUNNING;
        });
        BtParallel p = new BtParallel(BtParallel.Policy.REQUIRE_ALL,
            BtParallel.Policy.REQUIRE_ALL, List.of(one, two));
        p.tick(ctx());
        p.tick(ctx());

        assertEquals(2, oneTicks.get());
        assertEquals(2, twoTicks.get());
    }
}
