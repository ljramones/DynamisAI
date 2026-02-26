package org.dynamisai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DramaManagerConfigTest {

    @Test
    void defaultConfigConstructs() {
        DramaManagerConfig c = DramaManagerConfig.defaultConfig();
        assertNotNull(c);
    }

    @Test
    void thresholdsInValidRange() {
        DramaManagerConfig c = DramaManagerConfig.defaultConfig();
        assertTrue(c.escalationThreshold() >= 0f && c.escalationThreshold() <= 1f);
        assertTrue(c.reliefThreshold() >= 0f && c.reliefThreshold() <= 1f);
        assertTrue(c.tensionThreshold() >= 0f && c.tensionThreshold() <= 1f);
        assertTrue(c.lullThreshold() >= 0f && c.lullThreshold() <= 1f);
        assertTrue(c.maxAggressionMagnitude() >= 0f && c.maxAggressionMagnitude() <= 1f);
    }
}
