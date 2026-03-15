package org.dynamisengine.ai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngagementMetricsTest {

    @Test
    void fieldsClampedOnConstruction() {
        EngagementMetrics m = new EngagementMetrics(2f, -1f, 5f, -4f, 3f, 300L, 10L);
        assertEquals(1f, m.combatIntensity(), 1e-6f);
        assertEquals(0f, m.explorationRate(), 1e-6f);
        assertEquals(1f, m.decisionSpeed(), 1e-6f);
        assertEquals(0f, m.idleTime(), 1e-6f);
        assertEquals(1f, m.threatPressure(), 1e-6f);
    }

    @Test
    void neutralFactoryConstructs() {
        EngagementMetrics m = EngagementMetrics.neutral(123L);
        assertNotNull(m);
        assertEquals(123L, m.computedAtTick());
    }
}
