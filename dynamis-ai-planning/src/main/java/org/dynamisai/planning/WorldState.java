package org.dynamisai.planning;

import org.dynamisai.cognition.AffectVector;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.navigation.NavPoint;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.core.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Planner's read-only snapshot of the world.
 * Assembled from PerceptionSnapshot + MemoryStats + affect state.
 * The HTN planner reads only this — never WorldStateStore directly.
 */
public record WorldState(
    EntityId owner,
    long tick,
    AffectVector affect,
    ThreatLevel currentThreat,
    PerceptionSnapshot perception,
    MemoryStats memoryStats,
    Map<String, Object> blackboard,
    NavPoint agentPosition,
    NavPoint goalPosition,
    float distanceToGoal
) {
    /** Construct a WorldState with no active navigation (position at origin). */
    public static WorldState withoutNav(EntityId owner, long tick,
                                        AffectVector affect, ThreatLevel threat,
                                        PerceptionSnapshot perception,
                                        MemoryStats memoryStats,
                                        Map<String, Object> blackboard) {
        return new WorldState(owner, tick, affect, threat, perception, memoryStats,
            blackboard, NavPoint.of(0, 0, 0), NavPoint.of(0, 0, 0), 0f);
    }

    public boolean has(String key) {
        return blackboard.containsKey(key);
    }

    public Object get(String key) {
        return blackboard.get(key);
    }

    public boolean is(String key, Object value) {
        return value.equals(blackboard.get(key));
    }

    /** Merge propagated squad facts into a new immutable planning state. */
    public WorldState withSquadFacts(SquadBlackboard squadBlackboard, EntityId reader,
                                     Location readerPos, Location authorPos,
                                     long currentTick) {
        Map<String, Object> merged = new HashMap<>(blackboard);
        for (BlackboardEntry entry : squadBlackboard.snapshotEntries().values()) {
            if (entry.isAvailableTo(reader, readerPos, authorPos, currentTick)) {
                merged.put(entry.key(), entry.value());
            }
        }
        return new WorldState(owner, tick, affect, currentThreat, perception, memoryStats,
            Map.copyOf(merged), agentPosition, goalPosition, distanceToGoal);
    }
}
