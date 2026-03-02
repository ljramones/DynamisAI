package org.dynamisai.social;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RumorPropagatorTest {

    private static ReputationEvent witnessedEvent() {
        return new ReputationEvent(EntityId.of(10), EntityId.of(20), ReputationEventType.HARMED,
            1f, 60L, true, EntityId.of(30));
    }

    @Test
    void seedRumorInitialValues() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        Rumor r = p.seedRumor(witnessedEvent());
        assertEquals(0, r.hopCount());
        assertEquals(1f, r.fidelity(), 1e-6f);
    }

    @Test
    void postThenPropagateAppliesToRecipientRelationship() {
        ReputationEngine re = new ReputationEngine();
        RumorPropagator p = new RumorPropagator(re);
        SocialGraph graph = new SocialGraph();

        EntityId holder = EntityId.of(30);
        RumorQueue q = new RumorQueue();
        p.registerQueue(holder, q);

        Rumor rumor = p.seedRumor(witnessedEvent());
        p.post(holder, rumor);

        p.propagate(graph, 60L);

        Relationship rel = graph.get(EntityId.of(10), holder);
        assertTrue(rel.trust() < 0f);
    }

    @Test
    void maxHopRumorDiscarded() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();

        EntityId a = EntityId.of(1);
        RumorQueue qa = new RumorQueue();
        p.registerQueue(a, qa);

        Rumor rumor = new Rumor(UUID.randomUUID(), witnessedEvent(), a, a,
            RumorPropagator.MAX_HOP_COUNT, 1f, 1L, 1L);
        p.post(a, rumor);
        p.propagate(graph, 60L);

        assertEquals(0, p.pendingRumorCount());
    }

    @Test
    void lowFidelityRumorDiscarded() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();

        EntityId a = EntityId.of(1);
        RumorQueue qa = new RumorQueue();
        p.registerQueue(a, qa);

        Rumor rumor = new Rumor(UUID.randomUUID(), witnessedEvent(), a, a,
            0, RumorPropagator.MIN_FIDELITY - 0.01f, 1L, 1L);
        p.post(a, rumor);
        p.propagate(graph, 60L);

        assertEquals(0, p.pendingRumorCount());
    }

    @Test
    void propagatesOnlyToPositiveTrustNeighbors() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();

        EntityId a = EntityId.of(1);
        EntityId b = EntityId.of(2);
        EntityId c = EntityId.of(3);

        p.registerQueue(a, new RumorQueue());
        p.registerQueue(b, new RumorQueue());
        p.registerQueue(c, new RumorQueue());

        graph.set(a, b, Relationship.neutral(a, b).withTrust(0.6f));
        graph.set(a, c, Relationship.neutral(a, c).withTrust(-0.2f));

        p.post(a, p.seedRumor(witnessedEvent()));
        p.propagate(graph, 60L);

        Relationship bView = graph.get(EntityId.of(10), b);
        Relationship cView = graph.get(EntityId.of(10), c);
        assertTrue(bView.trust() < 0f, "Trusted neighbor should receive and apply rumor");
        assertEquals(0f, cView.trust(), 1e-6f, "Untrusted neighbor must not receive rumor");
    }

    @Test
    void secondPropagateDoesNotReapplyDrainedRumors() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();

        EntityId holder = EntityId.of(30);
        p.registerQueue(holder, new RumorQueue());
        p.post(holder, p.seedRumor(witnessedEvent()));

        p.propagate(graph, 60L);
        float afterFirst = graph.get(EntityId.of(10), holder).trust();
        p.propagate(graph, 120L);
        float afterSecond = graph.get(EntityId.of(10), holder).trust();

        assertEquals(afterFirst, afterSecond, 1e-6f);
    }

    @Test
    void pendingRumorCountReflectsQueues() {
        RumorPropagator p = new RumorPropagator(new ReputationEngine());
        EntityId a = EntityId.of(1);
        p.registerQueue(a, new RumorQueue());
        p.post(a, p.seedRumor(witnessedEvent()));
        assertEquals(1, p.pendingRumorCount());
    }
}
