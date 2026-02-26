package org.dynamisai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleInterruptionTest {

    @Test
    void noExpiryNeverExpires() {
        ScheduleInterruption i = new ScheduleInterruption("combat", 10, 1L, 0L);
        assertFalse(i.isExpired(10L));
    }

    @Test
    void expiresAtOrAfterTick() {
        ScheduleInterruption i = new ScheduleInterruption("combat", 10, 1L, 100L);
        assertTrue(i.isExpired(100L));
        assertTrue(i.isExpired(101L));
    }

    @Test
    void beforeExpiryIsNotExpired() {
        ScheduleInterruption i = new ScheduleInterruption("combat", 10, 1L, 100L);
        assertFalse(i.isExpired(99L));
    }
}
