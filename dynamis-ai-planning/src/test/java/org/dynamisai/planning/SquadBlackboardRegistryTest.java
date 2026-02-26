package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SquadBlackboardRegistryTest {

    @Test
    void getOrCreateReturnsSameInstance() {
        SquadBlackboardRegistry registry = new SquadBlackboardRegistry();
        SquadBlackboard a = registry.getOrCreate("alpha");
        SquadBlackboard b = registry.getOrCreate("alpha");
        assertSame(a, b);
    }

    @Test
    void disbandRemovesSquad() {
        SquadBlackboardRegistry registry = new SquadBlackboardRegistry();
        registry.getOrCreate("alpha");
        registry.disband("alpha");
        assertTrue(registry.get("alpha").isEmpty());
    }

    @Test
    void pruneAllPropagates() {
        SquadBlackboardRegistry registry = new SquadBlackboardRegistry();
        SquadBlackboard bb = registry.getOrCreate("alpha");
        bb.writeRadio("k", "v", EntityId.of(1L), 1L);
        registry.pruneAll(20L, 10L);
        assertTrue(bb.readDirect("k").isEmpty());
    }

    @Test
    void squadCountAccurate() {
        SquadBlackboardRegistry registry = new SquadBlackboardRegistry();
        registry.getOrCreate("a");
        registry.getOrCreate("b");
        assertEquals(2, registry.squadCount());
    }
}
