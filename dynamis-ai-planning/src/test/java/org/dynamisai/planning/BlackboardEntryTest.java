package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlackboardEntryTest {

    @Test
    void radioAlwaysAvailable() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 10L,
            PropagationChannel.RADIO, 0f);
        assertTrue(entry.isAvailableTo(EntityId.of(2L), new Location(0,0,0), new Location(999,0,999), 10L));
    }

    @Test
    void lineOfSightWithinThresholdAvailable() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 0L,
            PropagationChannel.LINE_OF_SIGHT, 0f);
        assertTrue(entry.isAvailableTo(EntityId.of(2L), new Location(0,0,0), new Location(10,0,0), 1L));
    }

    @Test
    void lineOfSightBeyondThresholdNotAvailable() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 0L,
            PropagationChannel.LINE_OF_SIGHT, 0f);
        assertFalse(entry.isAvailableTo(EntityId.of(2L), new Location(0,0,0), new Location(20,0,0), 1L));
    }

    @Test
    void runnerTooEarlyNotAvailable() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 0L,
            PropagationChannel.RUNNER, 2f);
        assertFalse(entry.isAvailableTo(EntityId.of(2L), new Location(10,0,0), new Location(0,0,0), 4L));
    }

    @Test
    void runnerSufficientTicksAvailable() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 0L,
            PropagationChannel.RUNNER, 2f);
        assertTrue(entry.isAvailableTo(EntityId.of(2L), new Location(10,0,0), new Location(0,0,0), 5L));
    }

    @Test
    void runnerScalesWithDistance() {
        BlackboardEntry entry = new BlackboardEntry("k", "v", EntityId.of(1L), 0L,
            PropagationChannel.RUNNER, 4f);
        assertFalse(entry.isAvailableTo(EntityId.of(2L), new Location(20,0,0), new Location(0,0,0), 4L));
        assertTrue(entry.isAvailableTo(EntityId.of(2L), new Location(20,0,0), new Location(0,0,0), 5L));
    }

    @Test
    void runnerRequiresPositiveSpeed() {
        assertThrows(IllegalArgumentException.class, () ->
            new BlackboardEntry("k", "v", EntityId.of(1L), 0L, PropagationChannel.RUNNER, 0f));
    }
}
