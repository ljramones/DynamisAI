package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.BeliefSource;
import org.dynamisengine.scripting.api.value.Percept;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeliefModelPerceptTest {

    private BeliefModel model;

    @BeforeEach
    void setUp() {
        model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
    }

    @Test
    void updateFromPerceptCreatesPerceptSourcedBelief() {
        Percept percept = Percept.of(EntityId.of(1L), "SIGHT.ENTITY", "guard", 0.8, 42L);

        model.updateFromPercept(percept);

        Belief belief = model.getBelief("percept.sight_entity").orElseThrow();
        assertEquals(BeliefSource.PERCEPT, belief.source());
    }

    @Test
    void updateFromPerceptUsesFidelityAsConfidence() {
        Percept percept = Percept.of(EntityId.of(1L), "SOUND.FOOTSTEP", "nearby", 0.37, 50L);

        model.updateFromPercept(percept);

        Belief belief = model.getBelief("percept.sound_footstep").orElseThrow();
        assertEquals(0.37f, belief.confidence(), 1e-6f);
    }

    @Test
    void updateFromPerceptKeyUsesLowercasedPerceptTypeConvention() {
        Percept percept = Percept.of(EntityId.of(1L), "ALERT", "danger", 1.0, 77L);

        model.updateFromPercept(percept);

        assertTrue(model.getBelief("percept.alert").isPresent());
    }
}
