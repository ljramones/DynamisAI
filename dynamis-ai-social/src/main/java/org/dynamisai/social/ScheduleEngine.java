package org.dynamisai.social;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.planning.WorldState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ScheduleEngine {

    private final Map<EntityId, NpcScheduleState> states = new ConcurrentHashMap<>();

    public void register(EntityId entityId, DailySchedule schedule) {
        states.put(entityId, new NpcScheduleState(entityId, schedule));
    }

    public void unregister(EntityId entityId) {
        states.remove(entityId);
    }

    public Optional<NpcScheduleState> stateFor(EntityId entityId) {
        return Optional.ofNullable(states.get(entityId));
    }

    public void update(int gameHour, long currentTick) {
        for (NpcScheduleState state : states.values()) {
            state.currentInterruption()
                .filter(i -> i.isExpired(currentTick))
                .ifPresent(i -> state.clearInterruption());

            ScheduledActivity activity = state.currentActivity(gameHour).orElse(null);
            state.tickNeeds(activity);
        }
    }

    public void interrupt(EntityId entityId, ScheduleInterruption interruption) {
        NpcScheduleState state = states.get(entityId);
        if (state == null) {
            return;
        }
        state.interrupt(interruption);
    }

    public void clearInterruption(EntityId entityId) {
        NpcScheduleState state = states.get(entityId);
        if (state != null) {
            state.clearInterruption();
        }
    }

    public WorldState buildSchedulePatch(EntityId entityId, WorldState base, int gameHour) {
        NpcScheduleState state = states.get(entityId);
        String activityName = "none";
        NeedType dominant = NeedType.SAFETY;
        boolean interrupted = false;
        Location targetLocation = new Location(0f, 0f, 0f);

        if (state != null) {
            ScheduledActivity activity = state.currentActivity(gameHour).orElse(null);
            activityName = activity != null ? activity.name() : "none";
            dominant = state.dominantNeed();
            interrupted = state.isInterrupted();
            targetLocation = activity != null ? activity.location() : targetLocation;
        }

        Map<String, Object> updated = new HashMap<>(base.blackboard());
        updated.put("schedule.currentActivity", activityName);
        updated.put("schedule.dominantNeed", dominant.name());
        updated.put("schedule.isInterrupted", interrupted);
        updated.put("schedule.targetLocation", targetLocation);

        return new WorldState(
            base.owner(), base.tick(), base.affect(),
            base.currentThreat(), base.perception(), base.memoryStats(),
            Map.copyOf(updated),
            base.agentPosition(), base.goalPosition(), base.distanceToGoal()
        );
    }

    public int registeredCount() {
        return states.size();
    }
}
