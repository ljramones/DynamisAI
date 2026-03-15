package org.dynamisengine.ai.social;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReputationDeltaTest {

    @Test
    void zeroFactoryReturnsEmptyDelta() {
        ReputationDelta d = ReputationDelta.zero();
        assertEquals(0f, d.trustDelta(), 1e-6f);
        assertEquals(0f, d.affinityDelta(), 1e-6f);
        assertTrue(d.tagsToAdd().isEmpty());
        assertTrue(d.tagsToRemove().isEmpty());
    }
}
