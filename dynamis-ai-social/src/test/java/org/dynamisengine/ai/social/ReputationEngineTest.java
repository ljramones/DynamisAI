package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReputationEngineTest {

    private static ReputationEvent event(ReputationEventType type, float magnitude, boolean witnessed) {
        return new ReputationEvent(EntityId.of(1), EntityId.of(2), type, magnitude, 1L, witnessed, EntityId.of(3));
    }

    @Test
    void helpedProducesPositiveDeltas() {
        ReputationDelta d = new ReputationEngine().compute(event(ReputationEventType.HELPED, 1f, true));
        assertTrue(d.trustDelta() > 0);
        assertTrue(d.affinityDelta() > 0);
    }

    @Test
    void betrayedProducesLargeNegativeTrust() {
        ReputationDelta d = new ReputationEngine().compute(event(ReputationEventType.BETRAYED, 1f, true));
        assertTrue(d.trustDelta() <= -0.4f + 1e-6f);
    }

    @Test
    void harmedMagnitudeOneMatchesScale() {
        ReputationDelta d = new ReputationEngine().compute(event(ReputationEventType.HARMED, 1f, true));
        assertEquals(-0.2f, d.trustDelta(), 1e-6f);
    }

    @Test
    void secondHandIsHalfOfWitnessed() {
        ReputationEngine engine = new ReputationEngine();
        ReputationDelta direct = engine.compute(event(ReputationEventType.THREATENED, 1f, true));
        ReputationDelta heard = engine.compute(event(ReputationEventType.THREATENED, 1f, false));

        assertEquals(direct.trustDelta() * 0.5f, heard.trustDelta(), 1e-6f);
        assertEquals(direct.affinityDelta() * 0.5f, heard.affinityDelta(), 1e-6f);
    }

    @Test
    void applyClampsTrustToRange() {
        SocialGraph graph = new SocialGraph();
        ReputationEngine engine = new ReputationEngine();
        ReputationEvent e = event(ReputationEventType.SAVED, 1f, true);
        for (int i = 0; i < 20; i++) {
            engine.apply(e, graph);
        }
        float trust = graph.get(EntityId.of(1), EntityId.of(2)).trust();
        assertTrue(trust <= 1f && trust >= -1f);
    }

    @Test
    void applyAddsAndRemovesTagsForBetrayed() {
        SocialGraph graph = new SocialGraph();
        graph.addTag(EntityId.of(1), EntityId.of(2), RelationshipTag.ALLY);

        ReputationEngine engine = new ReputationEngine();
        engine.apply(event(ReputationEventType.BETRAYED, 1f, true), graph);

        Relationship rel = graph.get(EntityId.of(1), EntityId.of(2));
        assertTrue(rel.hasTag(RelationshipTag.ENEMY));
        assertFalse(rel.hasTag(RelationshipTag.ALLY));
    }

    @Test
    void applyAllProcessesInTickOrder() {
        SocialGraph graph = new SocialGraph();
        ReputationEngine engine = new ReputationEngine();

        ReputationEvent first = new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
            1f, 2L, true, EntityId.of(1));
        ReputationEvent second = new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.HARMED,
            1f, 3L, true, EntityId.of(1));

        List<ReputationDelta> deltas = engine.applyAll(List.of(second, first), graph);
        assertEquals(2, deltas.size());
        float trust = graph.get(EntityId.of(1), EntityId.of(2)).trust();
        assertEquals(-0.1f, trust, 1e-4f);
    }

    @Test
    void customMagnitudeScalesOverrideDefaults() {
        ReputationEngine engine = new ReputationEngine(Map.of(ReputationEventType.HELPED, 2.0f));
        ReputationDelta d = engine.compute(event(ReputationEventType.HELPED, 1f, true));
        assertEquals(0.2f, d.trustDelta(), 1e-6f);
    }
}
