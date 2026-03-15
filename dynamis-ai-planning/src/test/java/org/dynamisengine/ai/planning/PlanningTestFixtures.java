package org.dynamisengine.ai.planning;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.memory.MemoryStats;
import org.dynamisengine.ai.perception.PerceptionSnapshot;

import java.util.Map;

final class PlanningTestFixtures {

    private PlanningTestFixtures() {}

    static WorldState state(EntityId owner, Map<String, Object> blackboard) {
        return WorldState.withoutNav(
            owner,
            1L,
            AffectVector.neutral(),
            ThreatLevel.NONE,
            PerceptionSnapshot.empty(owner, 1L, new Location(0, 0, 0)),
            new MemoryStats(owner, 0, 0, 0, 0, 0),
            blackboard
        );
    }
}
