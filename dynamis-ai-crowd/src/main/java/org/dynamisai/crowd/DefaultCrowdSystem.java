package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.navigation.NavPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default CrowdSystem implementation.
 */
public final class DefaultCrowdSystem implements CrowdSystem {

    private static final Logger log = LoggerFactory.getLogger(DefaultCrowdSystem.class);

    private static final float AGENT_MAX_SPEED = 4.0f;
    private static final float ARRIVAL_EPSILON = 0.05f;

    private final Map<GroupId, CrowdGroup> groups = new ConcurrentHashMap<>();

    // Maps entity → group for O(1) lookup
    private final Map<EntityId, GroupId> entityToGroup = new ConcurrentHashMap<>();

    private final LodController lodController;
    private volatile CrowdSnapshot latestSnapshot = CrowdSnapshot.empty(0L);

    public DefaultCrowdSystem() {
        this(new LodController());
    }

    public DefaultCrowdSystem(LodController lodController) {
        this.lodController = lodController;
    }

    @Override
    public GroupId createGroup(FormationType formation) {
        GroupId id = GroupId.next();
        groups.put(id, new CrowdGroup(id, formation));
        log.debug("CrowdGroup {} created ({})", id, formation);
        return id;
    }

    @Override
    public void addToGroup(GroupId groupId, EntityId entity, Location position) {
        CrowdGroup group = groups.get(groupId);
        if (group == null) {
            log.warn("addToGroup: unknown group {}", groupId);
            return;
        }
        removeFromGroup(entity);
        synchronized (group) {
            group.addAgent(CrowdAgent.of(entity, NavPoint.from(position)));
        }
        entityToGroup.put(entity, groupId);
    }

    @Override
    public void removeFromGroup(EntityId entity) {
        GroupId groupId = entityToGroup.remove(entity);
        if (groupId == null) return;
        CrowdGroup group = groups.get(groupId);
        if (group == null) return;
        synchronized (group) {
            group.removeAgent(entity);
        }
    }

    @Override
    public void dissolveGroup(GroupId groupId) {
        CrowdGroup group = groups.remove(groupId);
        if (group == null) return;
        synchronized (group) {
            group.agents().forEach(a -> entityToGroup.remove(a.id()));
        }
        log.debug("CrowdGroup {} dissolved", groupId);
    }

    @Override
    public void setGroupGoal(GroupId groupId, Location goal) {
        CrowdGroup group = groups.get(groupId);
        if (group != null) {
            group.setGoal(goal);
        }
    }

    @Override
    public void setFormation(GroupId groupId, FormationType formation) {
        CrowdGroup group = groups.get(groupId);
        if (group != null) {
            group.setFormation(formation);
        }
    }

    @Override
    public CrowdSnapshot tick(long tickNumber, float deltaT) {
        Map<GroupId, CrowdSnapshot.GroupSnapshot> snapshots = new LinkedHashMap<>();

        for (CrowdGroup group : groups.values()) {
            synchronized (group) {
                if (group.isEmpty()) continue;

                group.recomputeCentroid();

                CrowdLod lod = lodController.assignLod(group);
                group.setLod(lod);

                if (lod == CrowdLod.CULLED) {
                    snapshots.put(group.id(), buildGroupSnapshot(group));
                    continue;
                }

                if (lod == CrowdLod.FULL || lod == CrowdLod.REDUCED) {
                    List<FormationSlot> slots = group.computeSlots();
                    moveAgentsToSlots(group, slots, deltaT);
                }

                snapshots.put(group.id(), buildGroupSnapshot(group));
            }
        }

        CrowdSnapshot snapshot = new CrowdSnapshot(tickNumber,
            Collections.unmodifiableMap(snapshots));
        this.latestSnapshot = snapshot;
        return snapshot;
    }

    @Override
    public CrowdSnapshot latestSnapshot() { return latestSnapshot; }

    @Override
    public Optional<GroupId> groupOf(EntityId entity) {
        return Optional.ofNullable(entityToGroup.get(entity));
    }

    @Override
    public void setObserver(Location observer) {
        lodController.setObserver(NavPoint.from(observer));
    }

    @Override
    public int totalAgents() { return entityToGroup.size(); }

    private void moveAgentsToSlots(CrowdGroup group,
                                    List<FormationSlot> slots,
                                    float deltaT) {
        List<CrowdAgent> agents = group.agents();
        for (int i = 0; i < agents.size() && i < slots.size(); i++) {
            CrowdAgent agent = agents.get(i);
            FormationSlot slot = slots.get(i);

            NavPoint target = slot.worldPosition();
            float dist = agent.position().distanceTo(target);

            if (dist < ARRIVAL_EPSILON) {
                group.updateAgent(agent.withVelocity(NavPoint.of(0, 0, 0)));
                continue;
            }

            float speed = Math.min(AGENT_MAX_SPEED, dist / Math.max(deltaT, 0.001f));
            NavPoint dir = agent.position().directionTo(target);
            NavPoint vel = NavPoint.of(dir.x() * speed, 0, dir.z() * speed);

            NavPoint newPos = NavPoint.of(
                agent.position().x() + vel.x() * deltaT,
                agent.position().y(),
                agent.position().z() + vel.z() * deltaT);

            group.updateAgent(agent.withPosition(newPos).withVelocity(vel));
        }
    }

    private CrowdSnapshot.GroupSnapshot buildGroupSnapshot(CrowdGroup group) {
        List<CrowdSnapshot.AgentSnapshot> agentSnaps = new ArrayList<>();
        for (CrowdAgent a : group.agents()) {
            agentSnaps.add(new CrowdSnapshot.AgentSnapshot(
                a.id(), a.position(), a.velocity(),
                a.slotIndex(), a.isLeader()));
        }
        return new CrowdSnapshot.GroupSnapshot(
            group.id(), group.formation(), group.lod(),
            group.centroid(), group.goal(),
            Collections.unmodifiableList(agentSnaps));
    }
}
