package org.dynamisai.social;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RumorQueueTest {

    private Rumor rumor() {
        ReputationEvent e = new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
            1f, 1L, true, EntityId.of(3));
        return new Rumor(UUID.randomUUID(), e, EntityId.of(3), EntityId.of(3), 0, 1f, 1L, 1L);
    }

    @Test
    void enqueueThenDrainReturnsAll() {
        RumorQueue q = new RumorQueue();
        q.enqueue(rumor());
        q.enqueue(rumor());
        assertEquals(2, q.drain().size());
    }

    @Test
    void drainClearsQueue() {
        RumorQueue q = new RumorQueue();
        q.enqueue(rumor());
        q.drain();
        assertTrue(q.drain().isEmpty());
    }

    @Test
    void peekIsNonDestructive() {
        RumorQueue q = new RumorQueue();
        q.enqueue(rumor());
        assertEquals(1, q.peek().size());
        assertEquals(1, q.size());
    }
}
