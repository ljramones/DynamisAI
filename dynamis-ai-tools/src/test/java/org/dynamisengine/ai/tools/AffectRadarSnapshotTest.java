package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.cognition.AffectVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AffectRadarSnapshotTest {

    @Test
    void neutralAffectProducesLowAxes() {
        AffectRadarSnapshot s = AffectRadarSnapshot.from(AffectVector.neutral(), 0.2f, 1L);
        assertTrue(s.fear() >= 0f && s.fear() <= 1f);
        assertTrue(s.aggression() >= 0f && s.aggression() <= 1f);
    }

    @Test
    void maxFearFromNegativeValenceAndIntensity() {
        AffectRadarSnapshot s = AffectRadarSnapshot.from(new AffectVector(-1f, 1f, 0f, 0f, 1f), 0f, 1L);
        assertEquals(1f, s.fear(), 1e-6f);
    }

    @Test
    void maxAggressionFromDominanceAndIntensity() {
        AffectRadarSnapshot s = AffectRadarSnapshot.from(new AffectVector(0f, 0f, 1f, 0f, 1f), 0f, 1L);
        assertEquals(1f, s.aggression(), 1e-6f);
    }

    @Test
    void outputsClampedToRange() {
        AffectRadarSnapshot s = AffectRadarSnapshot.from(new AffectVector(-1f, 1f, 1f, 0f, 1f), 2f, 1L);
        assertTrue(s.fear() >= 0f && s.fear() <= 1f);
        assertTrue(s.suspicion() >= 0f && s.suspicion() <= 1f);
        assertTrue(s.curiosity() >= 0f && s.curiosity() <= 1f);
        assertTrue(s.aggression() >= 0f && s.aggression() <= 1f);
        assertTrue(s.loyalty() >= 0f && s.loyalty() <= 1f);
    }

    @Test
    void loyaltyUrgencyMapsDirectly() {
        AffectRadarSnapshot s = AffectRadarSnapshot.from(AffectVector.neutral(), 0.73f, 1L);
        assertEquals(0.73f, s.loyalty(), 1e-6f);
    }
}
