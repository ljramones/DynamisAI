package org.dynamisai.planning;

import org.dynamisai.cognition.AffectVector;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.perception.PerceptionSnapshot;

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
