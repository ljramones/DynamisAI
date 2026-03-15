package org.dynamisengine.ai.planning;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.memory.MemoryStats;
import org.dynamisengine.ai.perception.PerceptionSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorldStateIntegrationTest {

    private static WorldState baseState(EntityId owner) {
        return WorldState.withoutNav(owner, 1L, AffectVector.neutral(), ThreatLevel.NONE,
            PerceptionSnapshot.empty(owner, 1L, new Location(0, 0, 0)),
            new MemoryStats(owner, 0, 0, 0, 0, 0),
            Map.of("local.fact", true));
    }

    @Test
    void withSquadFactsMergesRadioFacts() {
        EntityId owner = EntityId.of(1L);
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("squad.threat.entity", EntityId.of(99L), EntityId.of(2L), 1L);

        WorldState enriched = baseState(owner).withSquadFacts(bb, owner,
            new Location(0,0,0), new Location(100,0,100), 1L);

        assertTrue(enriched.has("squad.threat.entity"));
    }

    @Test
    void withSquadFactsExcludesUnmetLos() {
        EntityId owner = EntityId.of(1L);
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("squad.rally.point", new Location(5,0,5), EntityId.of(2L),
            PropagationChannel.LINE_OF_SIGHT, 1L);

        WorldState enriched = baseState(owner).withSquadFacts(bb, owner,
            new Location(0,0,0), new Location(30,0,0), 1L);

        assertFalse(enriched.has("squad.rally.point"));
    }

    @Test
    void withSquadFactsReturnsNewInstance() {
        EntityId owner = EntityId.of(1L);
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("new.fact", 1, EntityId.of(2L), 1L);

        WorldState original = baseState(owner);
        WorldState enriched = original.withSquadFacts(bb, owner,
            new Location(0,0,0), new Location(0,0,0), 1L);

        assertNotSame(original, enriched);
        assertFalse(original.has("new.fact"));
    }

    @Test
    void mergedFactsAccessibleViaHasAndGet() {
        EntityId owner = EntityId.of(1L);
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("phase", "flank", EntityId.of(2L), 1L);

        WorldState enriched = baseState(owner).withSquadFacts(bb, owner,
            new Location(0,0,0), new Location(0,0,0), 1L);

        assertTrue(enriched.has("phase"));
        assertEquals("flank", enriched.get("phase"));
    }
}
