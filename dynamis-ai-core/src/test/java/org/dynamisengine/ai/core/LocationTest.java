package org.dynamisengine.ai.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationTest {

    @Test
    void distanceToComputesEuclideanDistance() {
        Location a = new Location(0f, 0f, 0f);
        Location b = new Location(3f, 4f, 0f);
        assertEquals(5f, a.distanceTo(b), 0.0001f);
    }

    @Test
    void bearingToNorthIsZero() {
        Location origin = new Location(0f, 0f, 0f);
        assertEquals(0f, origin.bearingTo(new Location(0f, 0f, -10f)), 0.0001f);
    }

    @Test
    void bearingToEastIsNinety() {
        Location origin = new Location(0f, 0f, 0f);
        assertEquals(90f, origin.bearingTo(new Location(10f, 0f, 0f)), 0.0001f);
    }
}
