package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeliefModelTest {

    private BeliefModel model;

    @BeforeEach
    void setUp() {
        model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
    }

    @Test
    void assertBeliefThenGetBelief() {
        model.assertBelief("player.location", "gate", 0.8f, 1L);
        assertTrue(model.getBelief("player.location").isPresent());
    }

    @Test
    void secondAssertOnSameKeyReinforces() {
        model.assertBelief("k", "v1", 0.2f, 1L);
        model.assertBelief("k", "v2", 0.9f, 2L);
        Belief b = model.getBelief("k").orElseThrow();
        assertEquals(0.9f, b.confidence(), 0.0001f);
        assertEquals(2L, b.lastReinforcedAtTick());
    }

    @Test
    void decayReducesConfidence() {
        model.assertBelief("k", "v", 0.5f, 1L);
        model.decay(2L);
        assertTrue(model.getBelief("k").orElseThrow().confidence() < 0.5f);
    }

    @Test
    void decayPrunesBelowMinimumRetained() {
        BeliefModel fast = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.fastDecay());
        fast.assertBelief("k", "v", 0.09f, 1L);
        fast.decay(2L);
        assertTrue(fast.getBelief("k").isEmpty());
    }

    @Test
    void staleBeliefsReturnsOnlyStale() {
        model.assertBelief("a", "v", 0.8f, 1L);
        assertEquals(1, model.staleBeliefs(400L).size());
        assertTrue(model.staleBeliefs(2L).isEmpty());
    }

    @Test
    void updateFromPerceptionVisualAssertsBelief() {
        FakePerceptionSnapshot snap = new FakePerceptionSnapshot(List.of(
            new FakePercept(EntityId.of(2L), "VISUAL", new Location(1f, 0f, 2f), 0.8f)
        ));
        model.updateFromPerception(snap, 10L);
        assertTrue(model.getBelief("entity.2.visible").isPresent());
    }

    @Test
    void updateFromPerceptionSameEntityReinforcesNotDuplicates() {
        FakePerceptionSnapshot snap = new FakePerceptionSnapshot(List.of(
            new FakePercept(EntityId.of(2L), "VISUAL", new Location(1f, 0f, 2f), 0.4f)
        ));
        model.updateFromPerception(snap, 10L);
        model.updateFromPerception(snap, 11L);
        assertEquals(1, model.beliefCount());
        assertEquals(11L, model.getBelief("entity.2.visible").orElseThrow().lastReinforcedAtTick());
    }

    @Test
    void beliefCountReflectsAddsAndPruning() {
        BeliefModel fast = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.fastDecay());
        fast.assertBelief("a", "v", 0.5f, 1L);
        fast.assertBelief("b", "v", 0.09f, 1L);
        fast.decay(2L);
        assertEquals(1, fast.beliefCount());
    }

    @Test
    void secondOrderAssertThenGet() {
        EntityId subject = EntityId.of(99L);
        model.assertSecondOrder(subject, "patrol.route", 0.7f, 5L);
        SecondOrderBelief b = model.getSecondOrder(subject, "patrol.route").orElseThrow();
        assertEquals(subject, b.subject());
        assertEquals(0.7f, b.estimatedConfidence(), 0.0001f);
    }

    record FakePerceptionSnapshot(List<FakePercept> percepts) {}

    record FakePercept(EntityId source, String stimulusType, Location location, float rawIntensity) {}
}
