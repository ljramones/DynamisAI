package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.navigation.NavPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A group of NPCs sharing a goal, formation, and LOD level.
 */
public final class CrowdGroup {

    private final GroupId id;
    private final List<CrowdAgent> agents;
    private FormationType formation;
    private NavPoint goal;
    private CrowdLod lod;
    private NavPoint centroid;

    public CrowdGroup(GroupId id, FormationType formation) {
        this.id = id;
        this.agents = new ArrayList<>();
        this.formation = formation;
        this.goal = NavPoint.of(0, 0, 0);
        this.lod = CrowdLod.FULL;
        this.centroid = NavPoint.of(0, 0, 0);
    }

    /** Add an agent. Assigns next available slot. */
    public void addAgent(CrowdAgent agent) {
        int slot = agents.size();
        boolean isLeader = (slot == 0);
        agents.add(agent.withSlot(slot, isLeader));
    }

    /** Remove an agent by id. Reindexes remaining agents. */
    public boolean removeAgent(EntityId id) {
        boolean removed = agents.removeIf(a -> a.id().equals(id));
        if (removed) reindexSlots();
        return removed;
    }

    public Optional<CrowdAgent> getAgent(EntityId id) {
        return agents.stream().filter(a -> a.id().equals(id)).findFirst();
    }

    /** Replace an agent in-place (same slot). */
    public void updateAgent(CrowdAgent updated) {
        for (int i = 0; i < agents.size(); i++) {
            if (agents.get(i).id().equals(updated.id())) {
                agents.set(i, updated);
                return;
            }
        }
    }

    public List<CrowdAgent> agents() { return Collections.unmodifiableList(agents); }
    public int size() { return agents.size(); }
    public boolean isEmpty() { return agents.isEmpty(); }

    /** Recompute centroid from current agent positions. */
    public void recomputeCentroid() {
        if (agents.isEmpty()) return;
        float sx = 0, sy = 0, sz = 0;
        for (CrowdAgent a : agents) {
            sx += a.position().x();
            sy += a.position().y();
            sz += a.position().z();
        }
        int n = agents.size();
        this.centroid = NavPoint.of(sx / n, sy / n, sz / n);
    }

    /**
     * Compute formation slots for current goal and centroid.
     * Returns empty list if group is empty or CULLED.
     */
    public List<FormationSlot> computeSlots() {
        if (agents.isEmpty() || lod == CrowdLod.CULLED) return List.of();
        NavPoint facing = centroid.distanceTo(goal) > 0.1f
            ? centroid.directionTo(goal)
            : NavPoint.of(0, 0, 1);
        return Formation.compute(formation, centroid, facing, agents.size());
    }

    public GroupId id() { return id; }
    public FormationType formation() { return formation; }
    public NavPoint goal() { return goal; }
    public CrowdLod lod() { return lod; }
    public NavPoint centroid() { return centroid; }

    public void setFormation(FormationType f) { this.formation = f; }
    public void setGoal(NavPoint goal) { this.goal = goal; }
    public void setGoal(Location loc) { this.goal = NavPoint.from(loc); }
    public void setLod(CrowdLod lod) { this.lod = lod; }

    /** Leader agent — slot 0. Empty if group has no agents. */
    public Optional<CrowdAgent> leader() {
        return agents.isEmpty() ? Optional.empty() : Optional.of(agents.get(0));
    }

    private void reindexSlots() {
        for (int i = 0; i < agents.size(); i++) {
            agents.set(i, agents.get(i).withSlot(i, i == 0));
        }
    }
}
