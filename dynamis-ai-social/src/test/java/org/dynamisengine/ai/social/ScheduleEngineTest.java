package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.planning.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleEngineTest {

    private static DailySchedule guard() {
        return DailySchedule.guardPatrol(new Location(10, 0, 10), new Location(0, 0, 0));
    }

    @Test
    void registerThenStateForPresent() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        assertTrue(engine.stateFor(e).isPresent());
    }

    @Test
    void updateTicksNeedsForAllRegistered() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        float before = engine.stateFor(e).orElseThrow().needs().get(NeedType.LOYALTY).urgency();
        engine.update(8, 1L);
        float after = engine.stateFor(e).orElseThrow().needs().get(NeedType.LOYALTY).urgency();
        assertNotEquals(before, after);
    }

    @Test
    void interruptSetsInterruption() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        engine.interrupt(e, new ScheduleInterruption("combat", 10, 1L, 0L));
        assertTrue(engine.stateFor(e).orElseThrow().isInterrupted());
    }

    @Test
    void clearInterruptionResumesSchedule() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        engine.interrupt(e, new ScheduleInterruption("combat", 10, 1L, 0L));
        engine.clearInterruption(e);
        assertFalse(engine.stateFor(e).orElseThrow().isInterrupted());
    }

    @Test
    void expiredInterruptionClearedOnUpdate() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        engine.interrupt(e, new ScheduleInterruption("combat", 10, 1L, 5L));
        engine.update(8, 5L);
        assertFalse(engine.stateFor(e).orElseThrow().isInterrupted());
    }

    @Test
    void buildSchedulePatchSetsAllKeys() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        WorldState base = ScheduleTestFixtures.baseWorldState(e);

        WorldState patched = engine.buildSchedulePatch(e, base, 8);

        assertTrue(patched.has("schedule.currentActivity"));
        assertTrue(patched.has("schedule.dominantNeed"));
        assertTrue(patched.has("schedule.isInterrupted"));
        assertTrue(patched.has("schedule.targetLocation"));
    }

    @Test
    void buildSchedulePatchForInterruptedNpcMarksInterruptedTrue() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId e = EntityId.of(1);
        engine.register(e, guard());
        engine.interrupt(e, new ScheduleInterruption("combat", 10, 1L, 0L));
        WorldState patched = engine.buildSchedulePatch(e, ScheduleTestFixtures.baseWorldState(e), 8);

        assertEquals(true, patched.get("schedule.isInterrupted"));
    }

    @Test
    void unregisteredEntityStateForEmpty() {
        ScheduleEngine engine = new ScheduleEngine();
        assertTrue(engine.stateFor(EntityId.of(404)).isEmpty());
    }

    @Test
    void registeredCountAccurate() {
        ScheduleEngine engine = new ScheduleEngine();
        EntityId a = EntityId.of(1);
        EntityId b = EntityId.of(2);
        engine.register(a, guard());
        engine.register(b, guard());
        assertEquals(2, engine.registeredCount());
        engine.unregister(a);
        assertEquals(1, engine.registeredCount());
    }
}
