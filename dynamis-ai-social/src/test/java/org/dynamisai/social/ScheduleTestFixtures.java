package org.dynamisai.social;

import org.dynamisai.cognition.AffectVector;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.planning.WorldState;

import java.util.Map;

final class ScheduleTestFixtures {

    private ScheduleTestFixtures() {}

    static WorldState baseWorldState(EntityId id) {
        return WorldState.withoutNav(
            id,
            1L,
            AffectVector.neutral(),
            ThreatLevel.NONE,
            PerceptionSnapshot.empty(id, 1L, new Location(0, 0, 0)),
            new MemoryStats(id, 0, 0, 0, 0, 0),
            Map.of()
        );
    }
}
