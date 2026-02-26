package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeliefContextBuilderTest {

    @Test
    void emptyModelReturnsHeaderOnly() {
        BeliefModel model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
        String text = BeliefContextBuilder.buildContext(model, 5);
        assertTrue(text.startsWith("Known facts"));
    }

    @Test
    void respectsMaxBeliefs() {
        BeliefModel model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
        for (int i = 0; i < 5; i++) {
            model.assertBelief("k" + i, i, 0.1f * (i + 1), i);
        }
        String text = BeliefContextBuilder.buildContext(model, 3);
        long lines = text.lines().filter(l -> l.startsWith("- ")).count();
        assertEquals(3, lines);
    }

    @Test
    void beliefsSortedByConfidenceDesc() {
        BeliefModel model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
        model.assertBelief("low", "v", 0.2f, 1L);
        model.assertBelief("high", "v", 0.9f, 1L);

        String text = BeliefContextBuilder.buildContext(model, 2);
        int high = text.indexOf("high");
        int low = text.indexOf("low");
        assertTrue(high >= 0 && low >= 0 && high < low);
    }

    @Test
    void outputContainsConfidenceValues() {
        BeliefModel model = new BeliefModel(EntityId.of(1L), BeliefDecayPolicy.defaultPolicy());
        model.assertBelief("k", "v", 0.75f, 1L);
        String text = BeliefContextBuilder.buildContext(model, 1);
        assertTrue(text.contains("[0.75]"));
    }
}
