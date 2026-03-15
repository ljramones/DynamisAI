package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.BeliefSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeliefTest {

    @Test
    void constructionValid() {
        Belief belief = new Belief("player.location", "gate", 0.7f, 1L, 1L, BeliefSource.INFERRED, EntityId.of(1L));
        assertEquals("player.location", belief.key());
    }

    @Test
    void confidenceOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Belief("k", "v", 1.2f, 1L, 1L, BeliefSource.INFERRED, EntityId.of(1L)));
    }

    @Test
    void decayedNeverBelowZero() {
        Belief belief = new Belief("k", "v", 0.05f, 1L, 1L, BeliefSource.INFERRED, EntityId.of(1L));
        assertEquals(0f, belief.decayed(1f).confidence(), 0.0001f);
    }

    @Test
    void reinforcedUpdatesConfidenceAndTick() {
        Belief belief = new Belief("k", "v", 0.2f, 1L, 1L, BeliefSource.INFERRED, EntityId.of(1L));
        Belief reinforced = belief.reinforced(0.8f, 10L);
        assertEquals(0.8f, reinforced.confidence(), 0.0001f);
        assertEquals(10L, reinforced.lastReinforcedAtTick());
    }

    @Test
    void isStaleWhenThresholdExceeded() {
        Belief belief = new Belief("k", "v", 0.5f, 1L, 2L, BeliefSource.INFERRED, EntityId.of(1L));
        assertTrue(belief.isStale(20L, 10L));
    }
}
