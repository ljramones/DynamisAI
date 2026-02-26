package org.dynamisai.social;

import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailyScheduleTest {

    @Test
    void activityAtReturnsMatchingEntry() {
        DailySchedule s = new DailySchedule(List.of(
            new ScheduledActivity("a", 8, 10, new Location(0, 0, 0), NeedType.STATUS, 1)
        ));
        assertEquals("a", s.activityAt(9).orElseThrow().name());
    }

    @Test
    void activityAtReturnsEmptyWhenNoMatch() {
        DailySchedule s = new DailySchedule(List.of(
            new ScheduledActivity("a", 8, 10, new Location(0, 0, 0), NeedType.STATUS, 1)
        ));
        assertTrue(s.activityAt(7).isEmpty());
    }

    @Test
    void guardPatrolFactoryCoversDayAndNight() {
        DailySchedule s = DailySchedule.guardPatrol(new Location(10, 0, 10), new Location(0, 0, 0));
        assertTrue(s.activityAt(7).isPresent());
        assertTrue(s.activityAt(23).isPresent());
    }

    @Test
    void merchantFactoryCoversShopAndHome() {
        DailySchedule s = DailySchedule.merchant(new Location(5, 0, 5), new Location(0, 0, 0));
        assertTrue(s.activityAt(9).isPresent());
        assertTrue(s.activityAt(21).isPresent());
    }

    @Test
    void activitiesReturnedInStartHourOrder() {
        DailySchedule s = new DailySchedule(List.of(
            new ScheduledActivity("b", 10, 11, new Location(0, 0, 0), NeedType.STATUS, 1),
            new ScheduledActivity("a", 8, 9, new Location(0, 0, 0), NeedType.STATUS, 1)
        ));
        assertEquals("a", s.activities().get(0).name());
        assertEquals("b", s.activities().get(1).name());
    }
}
