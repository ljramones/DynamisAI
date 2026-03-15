package org.dynamisengine.ai.social;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.memory.MemoryStats;
import org.dynamisengine.ai.perception.PerceptionSnapshot;
import org.dynamisengine.ai.planning.WorldState;

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
