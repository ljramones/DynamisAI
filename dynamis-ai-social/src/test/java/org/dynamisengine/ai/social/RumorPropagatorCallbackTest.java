package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RumorPropagatorCallbackTest {

    @Test
    void callbackInvokedWhenRumorPropagatesToTrustedNeighbor() {
        RumorPropagator propagator = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();
        EntityId holder = EntityId.of(1L);
        EntityId neighbor = EntityId.of(2L);

        propagator.registerQueue(holder, new RumorQueue());
        propagator.registerQueue(neighbor, new RumorQueue());
        graph.set(holder, neighbor, Relationship.neutral(holder, neighbor).withTrust(0.8f));

        AtomicInteger callbackCount = new AtomicInteger(0);
        propagator.setDeliveryCallback((recipient, rumor, senderTrust) -> callbackCount.incrementAndGet());
        propagator.post(holder, seedRumor(holder));

        propagator.propagate(graph, 60L);

        assertEquals(1, callbackCount.get());
    }

    @Test
    void callbackNotInvokedWhenTrustIsNonPositive() {
        RumorPropagator propagator = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();
        EntityId holder = EntityId.of(10L);
        EntityId neighbor = EntityId.of(20L);

        propagator.registerQueue(holder, new RumorQueue());
        propagator.registerQueue(neighbor, new RumorQueue());
        graph.set(holder, neighbor, Relationship.neutral(holder, neighbor).withTrust(0.0f));

        AtomicInteger callbackCount = new AtomicInteger(0);
        propagator.setDeliveryCallback((recipient, rumor, senderTrust) -> callbackCount.incrementAndGet());
        propagator.post(holder, seedRumor(holder));

        propagator.propagate(graph, 60L);

        assertEquals(0, callbackCount.get());
    }

    @Test
    void callbackNotInvokedWhenRumorAlreadyAtMaxHop() {
        RumorPropagator propagator = new RumorPropagator(new ReputationEngine());
        SocialGraph graph = new SocialGraph();
        EntityId holder = EntityId.of(100L);
        EntityId neighbor = EntityId.of(200L);

        propagator.registerQueue(holder, new RumorQueue());
        propagator.registerQueue(neighbor, new RumorQueue());
        graph.set(holder, neighbor, Relationship.neutral(holder, neighbor).withTrust(0.9f));

        AtomicInteger callbackCount = new AtomicInteger(0);
        propagator.setDeliveryCallback((recipient, rumor, senderTrust) -> callbackCount.incrementAndGet());
        propagator.post(holder, rumorAtMaxHop(holder));

        propagator.propagate(graph, 60L);

        assertEquals(0, callbackCount.get());
    }

    private static Rumor seedRumor(EntityId holder) {
        ReputationEvent event = new ReputationEvent(
            EntityId.of(301L),
            EntityId.of(302L),
            ReputationEventType.HARMED,
            1f,
            60L,
            true,
            holder
        );
        return new Rumor(UUID.randomUUID(), event, holder, holder, 0, 1f, 60L, 60L);
    }

    private static Rumor rumorAtMaxHop(EntityId holder) {
        ReputationEvent event = new ReputationEvent(
            EntityId.of(401L),
            EntityId.of(402L),
            ReputationEventType.HARMED,
            1f,
            60L,
            true,
            holder
        );
        return new Rumor(
            UUID.randomUUID(),
            event,
            holder,
            holder,
            RumorPropagator.MAX_HOP_COUNT,
            1f,
            60L,
            60L
        );
    }
}
