package org.dynamisengine.ai.lod;

import org.dynamisengine.ai.core.LodTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TickScalerTest {

    @Test
    void tier0TicksEveryFrame() {
        for (int i = 1; i <= 10; i++) {
            assertTrue(TickScaler.shouldTick(LodTier.TIER_0, i));
        }
    }

    @Test
    void tier1TicksEveryTwoFrames() {
        assertFalse(TickScaler.shouldTick(LodTier.TIER_1, 1));
        assertTrue(TickScaler.shouldTick(LodTier.TIER_1, 2));
    }

    @Test
    void tier2TicksEveryFourFrames() {
        assertTrue(TickScaler.shouldTick(LodTier.TIER_2, 4));
        assertFalse(TickScaler.shouldTick(LodTier.TIER_2, 5));
    }

    @Test
    void tier3TicksEverySixteenFrames() {
        assertTrue(TickScaler.shouldTick(LodTier.TIER_3, 16));
        assertFalse(TickScaler.shouldTick(LodTier.TIER_3, 17));
    }

    @Test
    void scaleRateMatchesTierDivisor() {
        assertEquals(60.0, TickScaler.scaleRate(60.0, LodTier.TIER_0), 0.0001);
        assertEquals(30.0, TickScaler.scaleRate(60.0, LodTier.TIER_1), 0.0001);
        assertEquals(15.0, TickScaler.scaleRate(60.0, LodTier.TIER_2), 0.0001);
        assertEquals(3.75, TickScaler.scaleRate(60.0, LodTier.TIER_3), 0.0001);
    }

    @Test
    void tier3TicksOnSixteenBoundary() {
        assertTrue(TickScaler.shouldTick(LodTier.TIER_3, 32));
    }
}
