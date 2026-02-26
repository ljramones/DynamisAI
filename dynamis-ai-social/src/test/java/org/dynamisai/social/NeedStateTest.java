package org.dynamisai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeedStateTest {

    @Test
    void defaultForAllTypesConstructs() {
        for (NeedType t : NeedType.values()) {
            NeedState s = NeedState.defaultFor(t);
            assertNotNull(s);
            assertEquals(t, s.type());
        }
    }

    @Test
    void tickMetReducesUrgencyByDecay() {
        NeedState s = new NeedState(NeedType.SAFETY, 0.5f, 0.1f, 0.2f);
        assertEquals(0.4f, s.tick(true).urgency(), 1e-6f);
    }

    @Test
    void tickUnmetIncreasesUrgencyByGrowth() {
        NeedState s = new NeedState(NeedType.SAFETY, 0.5f, 0.1f, 0.2f);
        assertEquals(0.7f, s.tick(false).urgency(), 1e-6f);
    }

    @Test
    void urgencyClampedAfterTicks() {
        NeedState high = new NeedState(NeedType.SAFETY, 0.95f, 0.1f, 0.2f);
        NeedState low = new NeedState(NeedType.SAFETY, 0.05f, 0.1f, 0.2f);
        assertEquals(1f, high.tick(false).urgency(), 1e-6f);
        assertEquals(0f, low.tick(true).urgency(), 1e-6f);
    }

    @Test
    void withUrgencyClampsRange() {
        NeedState s = NeedState.defaultFor(NeedType.CURIOSITY);
        assertEquals(1f, s.withUrgency(2f).urgency(), 1e-6f);
        assertEquals(0f, s.withUrgency(-2f).urgency(), 1e-6f);
    }
}
