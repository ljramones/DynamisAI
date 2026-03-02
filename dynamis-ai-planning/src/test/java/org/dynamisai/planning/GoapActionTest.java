package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoapActionTest {

    @Test
    void costReturnsBaseWhenDynamicMissing() {
        WorldState s = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapAction action = GoapAction.of("a", st -> true, List.of(), 0.7f);

        assertEquals(0.7f, action.cost(s), 1e-6f);
    }

    @Test
    void costUsesDynamicFunctionWhenProvided() {
        WorldState s = PlanningTestFixtures.state(EntityId.of(1L), Map.of("x", 1));
        GoapAction action = new GoapAction("a", st -> true, List.of(), 0.7f,
            st -> st.has("x") ? 0.2 : 0.9);

        assertEquals(0.2f, action.cost(s), 1e-6f);
    }

    @Test
    void applyReturnsNewStateWithEffectsApplied() {
        WorldState s = PlanningTestFixtures.state(EntityId.of(1L), Map.of("a", true));
        GoapAction action = GoapAction.of("a", st -> true,
            List.of(new PlannerEffect("b", 42)), 0.1f);

        WorldState next = action.apply(s);

        assertTrue(next.has("b"));
        assertEquals(42, next.get("b"));
        assertFalse(s.has("b"));
    }

    @Test
    void negativeBaseCostRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new GoapAction("x", st -> true, List.of(), -0.1f, null));
    }

    @Test
    void factoryConstructsCleanly() {
        GoapAction action = GoapAction.of("x", st -> true, List.of(), 0f);
        assertNotNull(action);
        assertEquals("x", action.name());
    }
}
