package org.dynamisai.social;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NpcScheduleStateTest {

    private static NpcScheduleState state() {
        DailySchedule schedule = new DailySchedule(List.of(
            new ScheduledActivity("patrol", 8, 12, new Location(1, 0, 1), NeedType.LOYALTY, 10)
        ));
        return new NpcScheduleState(EntityId.of(1), schedule);
    }

    @Test
    void currentActivityMatchesHour() {
        assertEquals("patrol", state().currentActivity(9).orElseThrow().name());
    }

    @Test
    void interruptSetsInterrupted() {
        NpcScheduleState s = state();
        s.interrupt(new ScheduleInterruption("combat", 10, 1L, 0L));
        assertTrue(s.isInterrupted());
    }

    @Test
    void clearInterruptionResetsFlag() {
        NpcScheduleState s = state();
        s.interrupt(new ScheduleInterruption("combat", 10, 1L, 0L));
        s.clearInterruption();
        assertFalse(s.isInterrupted());
    }

    @Test
    void tickNeedsMetNeedDecaysOthersGrow() {
        NpcScheduleState s = state();
        NeedState loyaltyBefore = s.needs().get(NeedType.LOYALTY);
        NeedState safetyBefore = s.needs().get(NeedType.SAFETY);

        s.tickNeeds(s.currentActivity(9).orElse(null));

        NeedState loyaltyAfter = s.needs().get(NeedType.LOYALTY);
        NeedState safetyAfter = s.needs().get(NeedType.SAFETY);
        assertTrue(loyaltyAfter.urgency() < loyaltyBefore.urgency());
        assertTrue(safetyAfter.urgency() >= safetyBefore.urgency());
    }

    @Test
    void tickNeedsInterruptedMeansAllGrow() {
        NpcScheduleState s = state();
        s.interrupt(new ScheduleInterruption("combat", 10, 1L, 0L));
        float loyaltyBefore = s.needs().get(NeedType.LOYALTY).urgency();
        s.tickNeeds(s.currentActivity(9).orElse(null));
        assertTrue(s.needs().get(NeedType.LOYALTY).urgency() >= loyaltyBefore);
    }

    @Test
    void dominantNeedReturnsHighestUrgency() {
        NpcScheduleState s = state();
        // push safety high by repeated unmet ticks.
        for (int i = 0; i < 20; i++) {
            s.tickNeeds(new ScheduledActivity("met-loyalty", 0, 24, new Location(0, 0, 0), NeedType.LOYALTY, 1));
        }
        assertEquals(NeedType.SAFETY, s.dominantNeed());
    }
}
