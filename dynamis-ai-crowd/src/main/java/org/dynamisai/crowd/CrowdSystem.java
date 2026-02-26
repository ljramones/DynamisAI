package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;

import java.util.Optional;

/**
 * Crowd system interface — manages all CrowdGroups, ticks formations,
 * applies LOD, and produces CrowdSnapshot each tick.
 */
public interface CrowdSystem {

    /** Create a new group with the given formation. Returns its GroupId. */
    GroupId createGroup(FormationType formation);

    /** Add an entity to an existing group at a given world position. */
    void addToGroup(GroupId group, EntityId entity, Location position);

    /** Remove an entity from its group. No-op if not in any group. */
    void removeFromGroup(EntityId entity);

    /** Remove an entire group and all its agents. */
    void dissolveGroup(GroupId group);

    /** Set the movement goal for a group. */
    void setGroupGoal(GroupId group, Location goal);

    /** Change formation type for a group. */
    void setFormation(GroupId group, FormationType formation);

    /** Tick all groups and produce a snapshot. */
    CrowdSnapshot tick(long tickNumber, float deltaT);

    /** Latest snapshot — may be one tick stale if called between ticks. */
    CrowdSnapshot latestSnapshot();

    /** Find which group an entity belongs to. */
    Optional<GroupId> groupOf(EntityId entity);

    /** Update observer position for LOD calculation. */
    void setObserver(Location observer);

    /** Total agents across all groups. */
    int totalAgents();
}
