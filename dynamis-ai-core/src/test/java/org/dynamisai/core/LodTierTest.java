package org.dynamisai.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LodTierTest {

    @Test
    void enumOrderIsTier0ThroughTier3() {
        assertEquals(LodTier.TIER_0, LodTier.values()[0]);
        assertEquals(LodTier.TIER_1, LodTier.values()[1]);
        assertEquals(LodTier.TIER_2, LodTier.values()[2]);
        assertEquals(LodTier.TIER_3, LodTier.values()[3]);
    }
}

