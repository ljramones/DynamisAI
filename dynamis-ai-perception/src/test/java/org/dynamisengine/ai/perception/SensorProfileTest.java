package org.dynamisengine.ai.perception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorProfileTest {

    @Test
    void defaultHumanConstructs() {
        SensorProfile profile = SensorProfile.defaultHuman();
        assertEquals(30f, profile.visionRadiusMetres(), 0.0001f);
    }

    @Test
    void blindHasZeroVision() {
        SensorProfile profile = SensorProfile.blind();
        assertEquals(0f, profile.visionRadiusMetres(), 0.0001f);
    }

    @Test
    void deafHasZeroHearing() {
        SensorProfile profile = SensorProfile.deaf();
        assertEquals(0f, profile.hearingRadiusMetres(), 0.0001f);
    }

    @Test
    void negativeVisionRadiusRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new SensorProfile(-1f, 120f, 20f, 0.5f, 0f));
    }

    @Test
    void acuityOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new SensorProfile(30f, 120f, 20f, 1.2f, 0f));
    }

    @Test
    void angleGreaterThan360Rejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new SensorProfile(30f, 361f, 20f, 0.5f, 0f));
    }
}
