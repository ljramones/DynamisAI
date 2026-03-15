package org.dynamisengine.ai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorModifierEventTest {

    @Test
    void validConstruction() {
        BehaviorModifierEvent e = new BehaviorModifierEvent(
            BehaviorModifierType.AMBIENT_DENSITY, 0.5f, null, "ambient.density", 1L);
        assertEquals(0.5f, e.magnitude(), 1e-6f);
    }

    @Test
    void magnitudeOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new BehaviorModifierEvent(BehaviorModifierType.AMBIENT_DENSITY, -0.1f, null, "x", 1L));
        assertThrows(IllegalArgumentException.class, () ->
            new BehaviorModifierEvent(BehaviorModifierType.AMBIENT_DENSITY, 1.1f, null, "x", 1L));
    }

    @Test
    void targetFactionMayBeNull() {
        assertDoesNotThrow(() ->
            new BehaviorModifierEvent(BehaviorModifierType.AMBIENT_DENSITY, 0.2f, null, "x", 1L));
    }
}
