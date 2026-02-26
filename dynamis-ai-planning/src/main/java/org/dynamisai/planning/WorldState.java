package org.dynamisai.planning;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.navigation.NavPoint;
import org.dynamisai.perception.PerceptionSnapshot;

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
}
