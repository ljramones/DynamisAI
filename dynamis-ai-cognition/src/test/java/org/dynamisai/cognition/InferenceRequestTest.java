package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InferenceRequestTest {

    private static DialogueRequest sampleDialogue() {
        return new DialogueRequest(
            EntityId.of(1L),
            EntityId.of(2L),
            "Hello",
            new WorldFacts(Map.of(), List.of(), ThreatLevel.NONE, new Location(0, 0, 0), null),
            AffectVector.neutral(),
            null
        );
    }

    @Test
    void unseededFactoryDisablesSeeding() {
        InferenceRequest request = InferenceRequest.unseeded(sampleDialogue());
        assertFalse(request.seedingEnabled());
        assertEquals(0L, request.deterministicSeed());
    }

    @Test
    void seededFactoryEnablesSeeding() {
        InferenceRequest request = InferenceRequest.seeded(sampleDialogue(), 9876L);
        assertTrue(request.seedingEnabled());
        assertEquals(9876L, request.deterministicSeed());
    }

    @Test
    void recordEqualityWorks() {
        InferenceRequest a = InferenceRequest.seeded(sampleDialogue(), 77L);
        InferenceRequest b = InferenceRequest.seeded(sampleDialogue(), 77L);
        assertEquals(a, b);
    }
}

