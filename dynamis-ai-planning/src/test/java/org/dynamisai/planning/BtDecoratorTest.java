package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BtDecoratorTest {

    private BtContext ctx() {
        return new BtContext(EntityId.of(1L),
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()),
            null, 1L, 5L);
    }

    @Test
    void inverterFlipsTerminalStatesAndPassesRunning() {
        assertEquals(BtStatus.FAILURE,
            new BtDecorator(BtDecorator.Type.INVERTER, new BtAction("a", c -> BtStatus.SUCCESS)).tick(ctx()));
        assertEquals(BtStatus.SUCCESS,
            new BtDecorator(BtDecorator.Type.INVERTER, new BtAction("a", c -> BtStatus.FAILURE)).tick(ctx()));
        assertEquals(BtStatus.RUNNING,
            new BtDecorator(BtDecorator.Type.INVERTER, new BtAction("a", c -> BtStatus.RUNNING)).tick(ctx()));
    }

    @Test
    void succeederAlwaysSuccess() {
        BtDecorator d = new BtDecorator(BtDecorator.Type.SUCCEEDER,
            new BtAction("a", c -> BtStatus.FAILURE));
        assertEquals(BtStatus.SUCCESS, d.tick(ctx()));
    }

    @Test
    void repeaterTicksNTimesThenSuccess() {
        AtomicInteger ticks = new AtomicInteger();
        BtDecorator d = new BtDecorator(BtDecorator.Type.REPEATER, 3,
            new BtAction("a", c -> {
                ticks.incrementAndGet();
                return BtStatus.SUCCESS;
            }));

        assertEquals(BtStatus.RUNNING, d.tick(ctx()));
        assertEquals(BtStatus.RUNNING, d.tick(ctx()));
        assertEquals(BtStatus.SUCCESS, d.tick(ctx()));
        assertEquals(3, ticks.get());
    }

    @Test
    void untilFailRepeatsUntilFailure() {
        AtomicInteger ticks = new AtomicInteger();
        BtDecorator d = new BtDecorator(BtDecorator.Type.UNTIL_FAIL,
            new BtAction("a", c -> ticks.getAndIncrement() < 2 ? BtStatus.SUCCESS : BtStatus.FAILURE));

        assertEquals(BtStatus.RUNNING, d.tick(ctx()));
        assertEquals(BtStatus.RUNNING, d.tick(ctx()));
        assertEquals(BtStatus.FAILURE, d.tick(ctx()));
    }
}
