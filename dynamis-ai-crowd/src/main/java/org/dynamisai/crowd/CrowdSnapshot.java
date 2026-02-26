package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.navigation.NavPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable tick snapshot of all crowd state.
 */
public record CrowdSnapshot(
    long tick,
    Map<GroupId, GroupSnapshot> groups
) {
    public CrowdSnapshot {
        Map<GroupId, GroupSnapshot> copy = new LinkedHashMap<>();
        for (Map.Entry<GroupId, GroupSnapshot> e : groups.entrySet()) {
            GroupSnapshot g = e.getValue();
            copy.put(e.getKey(), new GroupSnapshot(
                g.id(),
                g.formation(),
                g.lod(),
                g.centroid(),
                g.goal(),
                List.copyOf(g.agents())
            ));
        }
        groups = Collections.unmodifiableMap(copy);
    }

    public record GroupSnapshot(
        GroupId id,
        FormationType formation,
        CrowdLod lod,
        NavPoint centroid,
        NavPoint goal,
        List<AgentSnapshot> agents
    ) {
        public GroupSnapshot {
            agents = List.copyOf(agents);
        }
    }

    public record AgentSnapshot(
        EntityId id,
        NavPoint position,
        NavPoint velocity,
        int slotIndex,
        boolean isLeader
    ) {}

    /** Find the group an agent belongs to in this snapshot. */
    public Optional<GroupSnapshot> groupOf(EntityId agentId) {
        return groups.values().stream()
            .filter(g -> g.agents().stream().anyMatch(a -> a.id().equals(agentId)))
            .findFirst();
    }

    /** Find an agent's snapshot across all groups. */
    public Optional<AgentSnapshot> findAgent(EntityId id) {
        return groups.values().stream()
            .flatMap(g -> g.agents().stream())
            .filter(a -> a.id().equals(id))
            .findFirst();
    }

    public static CrowdSnapshot empty(long tick) {
        return new CrowdSnapshot(tick, Map.of());
    }

    /** Total agents across all groups in this snapshot. */
    public int totalAgents() {
        return groups.values().stream().mapToInt(g -> g.agents().size()).sum();
    }
}
