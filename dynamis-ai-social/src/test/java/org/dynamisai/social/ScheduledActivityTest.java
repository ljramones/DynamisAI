package org.dynamisai.social;

import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledActivityTest {

    @Test
    void validConstruction() {
        ScheduledActivity a = new ScheduledActivity("patrol", 8, 12,
            new Location(1, 0, 2), NeedType.LOYALTY, 10);
        assertEquals("patrol", a.name());
    }

    @Test
    void endHourNotAfterStartRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new ScheduledActivity("x", 10, 10, new Location(0, 0, 0), NeedType.SAFETY, 1));
        assertThrows(IllegalArgumentException.class, () ->
            new ScheduledActivity("x", 10, 9, new Location(0, 0, 0), NeedType.SAFETY, 1));
    }

    @Test
    void startHourOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new ScheduledActivity("x", -1, 5, new Location(0, 0, 0), NeedType.SAFETY, 1));
        assertThrows(IllegalArgumentException.class, () ->
            new ScheduledActivity("x", 24, 25, new Location(0, 0, 0), NeedType.SAFETY, 1));
    }
}
