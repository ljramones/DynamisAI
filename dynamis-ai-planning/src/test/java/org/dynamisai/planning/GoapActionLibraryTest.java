package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoapActionLibraryTest {

    @Test
    void registerAndFind() {
        GoapActionLibrary lib = new GoapActionLibrary();
        GoapAction a = GoapAction.of("a", s -> true, List.of(), 0.1f);
        lib.register(a);

        assertTrue(lib.find("a").isPresent());
        assertEquals(a, lib.find("a").orElseThrow());
    }

    @Test
    void applicableReturnsOnlyPassingPreconditions() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("yes", s -> s.has("x"), List.of(), 0.1f));
        lib.register(GoapAction.of("no", s -> s.has("y"), List.of(), 0.1f));

        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of("x", true));
        List<GoapAction> applicable = lib.applicableTo(state);

        assertEquals(1, applicable.size());
        assertEquals("yes", applicable.get(0).name());
    }

    @Test
    void unregisterRemovesAction() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(), 0.1f));
        lib.unregister("a");

        assertTrue(lib.find("a").isEmpty());
    }

    @Test
    void allReturnsDefensiveCopy() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(), 0.1f));

        List<GoapAction> all = lib.all();
        assertThrows(UnsupportedOperationException.class,
            () -> all.add(GoapAction.of("b", s -> true, List.of(), 0.1f)));
        assertEquals(1, lib.size());
    }
}
