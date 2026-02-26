package org.dynamisai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngagementTrackerTest {

    @Test
    void freshTrackerComputeNoException() {
        EngagementTracker t = new EngagementTracker();
        assertDoesNotThrow(() -> t.compute(1L));
    }

    @Test
    void allCombatTicksNearOne() {
        EngagementTracker t = new EngagementTracker();
        for (int i = 0; i < EngagementTracker.WINDOW_TICKS; i++) {
            t.recordCombatTick(true);
            t.recordExploration(false);
            t.recordThreatPressure(0.2f);
            t.recordDecisionSpeed(1f);
        }
        assertTrue(t.compute(1L).combatIntensity() > 0.95f);
    }

    @Test
    void noCombatTicksNearZero() {
        EngagementTracker t = new EngagementTracker();
        for (int i = 0; i < EngagementTracker.WINDOW_TICKS; i++) {
            t.recordCombatTick(false);
            t.recordExploration(false);
            t.recordThreatPressure(0.2f);
            t.recordDecisionSpeed(1f);
        }
        assertTrue(t.compute(1L).combatIntensity() < 0.05f);
    }

    @Test
    void slidingWindowDropsOldData() {
        EngagementTracker t = new EngagementTracker();
        for (int i = 0; i < EngagementTracker.WINDOW_TICKS; i++) {
            t.recordCombatTick(true);
            t.recordExploration(false);
            t.recordThreatPressure(0.2f);
            t.recordDecisionSpeed(1f);
        }
        for (int i = 0; i < EngagementTracker.WINDOW_TICKS; i++) {
            t.recordCombatTick(false);
            t.recordExploration(false);
            t.recordThreatPressure(0.2f);
            t.recordDecisionSpeed(1f);
        }
        assertTrue(t.compute(2L).combatIntensity() < 0.05f);
    }

    @Test
    void threatPressureReflectedInCompute() {
        EngagementTracker t = new EngagementTracker();
        for (int i = 0; i < 100; i++) {
            t.recordCombatTick(false);
            t.recordExploration(false);
            t.recordThreatPressure(0.9f);
            t.recordDecisionSpeed(0.5f);
        }
        assertTrue(t.compute(3L).threatPressure() > 0.85f);
    }
}
