package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeliefModelRegistryTest {

    @Test
    void getOrCreateReturnsSameInstance() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        BeliefModel a = registry.getOrCreate(EntityId.of(1L));
        BeliefModel b = registry.getOrCreate(EntityId.of(1L));
        assertSame(a, b);
    }

    @Test
    void removeClearsModel() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        EntityId id = EntityId.of(2L);
        registry.getOrCreate(id);
        registry.remove(id);
        assertTrue(registry.get(id).isEmpty());
    }

    @Test
    void decayAllPropagates() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.fastDecay());
        BeliefModel model = registry.getOrCreate(EntityId.of(3L));
        model.assertBelief("k", "v", 0.5f, 1L);
        registry.decayAll(2L);
        assertTrue(model.getBelief("k").orElseThrow().confidence() < 0.5f);
    }

    @Test
    void registeredCountAccurate() {
        BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        registry.getOrCreate(EntityId.of(1L));
        registry.getOrCreate(EntityId.of(2L));
        assertEquals(2, registry.registeredCount());
    }
}
