package org.dynamisai.cognition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeliefDecayPolicyTest {

    @Test
    void defaultPolicyMatchesSpec() {
        BeliefDecayPolicy p = BeliefDecayPolicy.defaultPolicy();
        assertEquals(0.001f, p.decayPerTick(), 0.00001f);
        assertEquals(300L, p.stalenessThresholdTicks());
        assertEquals(0.05f, p.minimumRetainedConfidence(), 0.00001f);
    }

    @Test
    void fastDecayMatchesSpec() {
        BeliefDecayPolicy p = BeliefDecayPolicy.fastDecay();
        assertEquals(0.01f, p.decayPerTick(), 0.00001f);
        assertEquals(60L, p.stalenessThresholdTicks());
        assertEquals(0.1f, p.minimumRetainedConfidence(), 0.00001f);
    }

    @Test
    void persistentMatchesSpec() {
        BeliefDecayPolicy p = BeliefDecayPolicy.persistent();
        assertEquals(0.0001f, p.decayPerTick(), 0.00001f);
        assertEquals(3000L, p.stalenessThresholdTicks());
        assertEquals(0.01f, p.minimumRetainedConfidence(), 0.00001f);
    }
}
