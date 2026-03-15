package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.cognition.Belief;
import org.dynamisengine.ai.cognition.BeliefDecayPolicy;
import org.dynamisengine.ai.cognition.BeliefModel;
import org.dynamisengine.ai.cognition.BeliefModelRegistry;
import org.dynamisengine.ai.core.BeliefSource;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RumorIngestionHandlerTest {

    @Test
    void deliveredRumorCreatesBeliefWithScaledConfidenceAndRumorSource() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        RumorIngestionHandler handler = new RumorIngestionHandler(registry);
        EntityId recipient = EntityId.of(1L);
        EntityId actor = EntityId.of(2L);
        Rumor rumor = rumor(recipient, actor, 0.8f, 55L);

        handler.onDelivered(recipient, rumor, 0.5f);

        String key = "rumor.harmed.actor." + actor.id();
        BeliefModel model = registry.getOrCreate(recipient);
        Optional<Belief> belief = model.getBelief(key);
        assertTrue(belief.isPresent());
        assertEquals(0.4f, belief.orElseThrow().confidence(), 1e-6f);
        assertEquals(BeliefSource.RUMOR, belief.orElseThrow().source());
    }

    @Test
    void zeroConfidenceRumorIsDiscarded() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        RumorIngestionHandler handler = new RumorIngestionHandler(registry);
        EntityId recipient = EntityId.of(10L);
        EntityId actor = EntityId.of(20L);
        Rumor rumor = rumor(recipient, actor, 0.1f, 77L);

        handler.onDelivered(recipient, rumor, 0.0f);

        String key = "rumor.harmed.actor." + actor.id();
        BeliefModel model = registry.getOrCreate(recipient);
        assertTrue(model.getBelief(key).isEmpty());
    }

    @Test
    void fullConfidenceRumorCreatesBeliefAtOne() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        RumorIngestionHandler handler = new RumorIngestionHandler(registry);
        EntityId recipient = EntityId.of(100L);
        EntityId actor = EntityId.of(200L);
        Rumor rumor = rumor(recipient, actor, 1.0f, 99L);

        handler.onDelivered(recipient, rumor, 1.0f);

        String key = "rumor.harmed.actor." + actor.id();
        Belief belief = registry.getOrCreate(recipient).getBelief(key).orElseThrow();
        assertEquals(1.0f, belief.confidence(), 1e-6f);
        assertEquals(key, belief.key());
    }

    private static Rumor rumor(EntityId recipient, EntityId actor, float fidelity, long tick) {
        ReputationEvent sourceEvent = new ReputationEvent(
            actor,
            recipient,
            ReputationEventType.HARMED,
            0.9f,
            tick,
            true,
            EntityId.of(3L)
        );
        return new Rumor(
            UUID.randomUUID(),
            sourceEvent,
            EntityId.of(3L),
            EntityId.of(3L),
            0,
            fidelity,
            tick,
            tick
        );
    }
}
