package org.dynamisai.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BakeScenarioTest {

    @Test
    void quickHasDefaults() {
        BakeScenario scenario = BakeScenario.quick("test", 100);
        assertEquals(3, scenario.assertions().size());
        assertEquals(42L, scenario.deterministicSeed());
    }

    @Test
    void fullHasDefaults() {
        BakeScenario scenario = BakeScenario.full("test");
        assertEquals(10_000, scenario.tickCount());
        assertEquals(5, scenario.assertions().size());
        assertEquals(42L, scenario.deterministicSeed());
    }
}
