package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.core.WorldFacts;
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

