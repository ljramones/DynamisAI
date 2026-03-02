package org.dynamisai.social;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReputationEventTest {

    @Test
    void validConstruction() {
        ReputationEvent e = new ReputationEvent(
            EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
            0.7f, 10L, true, EntityId.of(3));
        assertEquals(0.7f, e.magnitude(), 1e-6f);
    }

    @Test
    void magnitudeOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
                -0.1f, 1L, true, EntityId.of(1)));
        assertThrows(IllegalArgumentException.class, () ->
            new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
                1.1f, 1L, true, EntityId.of(1)));
    }
}
