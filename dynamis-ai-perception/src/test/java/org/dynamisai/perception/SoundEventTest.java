package org.dynamisai.perception;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SoundEventTest {

    @Test
    void recordConstructionAndAccess() {
        SoundEvent event = new SoundEvent(
            EntityId.of(1L),
            new Location(1f, 0f, 2f),
            0.7f,
            StimulusType.AUDITORY,
            10L);

        assertEquals(1L, event.sourceEntityId().id());
        assertEquals(0.7f, event.intensity(), 0.0001f);
        assertEquals(StimulusType.AUDITORY, event.type());
    }

    @Test
    void recordEquality() {
        SoundEvent a = new SoundEvent(EntityId.of(1L), new Location(0f, 0f, 0f),
            0.5f, StimulusType.SOCIAL, 2L);
        SoundEvent b = new SoundEvent(EntityId.of(1L), new Location(0f, 0f, 0f),
            0.5f, StimulusType.SOCIAL, 2L);
        assertEquals(a, b);
    }

    @Test
    void intensityOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new SoundEvent(EntityId.of(1L), new Location(0f, 0f, 0f),
                2f, StimulusType.AUDITORY, 1L));
    }
}
