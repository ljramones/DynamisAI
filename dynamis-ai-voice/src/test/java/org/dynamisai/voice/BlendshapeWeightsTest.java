package org.dynamisai.voice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BlendshapeWeightsTest {

    @Test
    void validConstructionAndGet() {
        BlendshapeWeights w = new BlendshapeWeights(Map.of("jawOpen", 0.5f));
        assertEquals(0.5f, w.get("jawOpen"), 0.0001f);
        assertEquals(0f, w.get("missing"), 0.0001f);
    }

    @Test
    void outOfRangeRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new BlendshapeWeights(Map.of("jawOpen", 1.1f)));
    }

    @Test
    void returnedMapIsImmutable() {
        Map<String, Float> src = new HashMap<>();
        src.put("jawOpen", 0.4f);
        BlendshapeWeights w = new BlendshapeWeights(src);
        src.put("jawOpen", 0.9f);
        assertEquals(0.4f, w.get("jawOpen"), 0.0001f);
        assertThrows(UnsupportedOperationException.class,
            () -> w.weights().put("x", 0.1f));
    }
}
