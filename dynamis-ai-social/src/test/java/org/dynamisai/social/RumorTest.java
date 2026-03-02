package org.dynamisai.social;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RumorTest {

    private Rumor seed() {
        ReputationEvent e = new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
            1f, 1L, true, EntityId.of(3));
        return new Rumor(UUID.randomUUID(), e, EntityId.of(3), EntityId.of(3), 0, 1f, 1L, 1L);
    }

    @Test
    void propagateToIncrementsHopCount() {
        Rumor r = seed().propagateTo(EntityId.of(4), 2L);
        assertEquals(1, r.hopCount());
    }

    @Test
    void propagateToReducesFidelity() {
        Rumor r = seed().propagateTo(EntityId.of(4), 2L);
        assertEquals(1f - Rumor.FIDELITY_DECAY_PER_HOP, r.fidelity(), 1e-6f);
    }

    @Test
    void fidelityNeverBelowZero() {
        Rumor r = seed();
        for (int i = 0; i < 20; i++) {
            r = r.propagateTo(EntityId.of(100 + i), 2L + i);
        }
        assertTrue(r.fidelity() >= 0f);
    }

    @Test
    void currentHolderChangesOnPropagation() {
        Rumor r = seed().propagateTo(EntityId.of(9), 10L);
        assertEquals(EntityId.of(9), r.currentHolder());
    }
}
