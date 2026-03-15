package org.dynamisengine.ai.perception;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundEventQueueTest {

    @Test
    void postThenDrainReturnsEvents() {
        SoundEventQueue queue = new SoundEventQueue();
        SoundEvent event = event(1L);
        queue.post(event);

        List<SoundEvent> drained = queue.drainForTick(1L);
        assertEquals(1, drained.size());
        assertEquals(event, drained.get(0));
    }

    @Test
    void drainClearsBuffer() {
        SoundEventQueue queue = new SoundEventQueue();
        queue.post(event(1L));

        queue.drainForTick(1L);
        assertTrue(queue.drainForTick(1L).isEmpty());
    }

    @Test
    void peekIsNonDestructive() {
        SoundEventQueue queue = new SoundEventQueue();
        queue.post(event(1L));

        assertFalse(queue.peek().isEmpty());
        assertFalse(queue.drainForTick(1L).isEmpty());
    }

    @Test
    void drainReturnsAllQueuedEventsRegardlessOfTick() {
        SoundEventQueue queue = new SoundEventQueue();
        queue.post(event(1L));
        queue.post(event(2L));

        List<SoundEvent> drained = queue.drainForTick(999L);
        assertEquals(2, drained.size());
    }

    private static SoundEvent event(long tick) {
        return new SoundEvent(EntityId.of(1L), new Location(0f, 0f, 0f),
            0.5f, StimulusType.AUDITORY, tick);
    }
}
