package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class NpcScheduleState {

    private final EntityId entityId;
    private final DailySchedule schedule;
    private final EnumMap<NeedType, NeedState> needs = new EnumMap<>(NeedType.class);
    private ScheduleInterruption interruption;

    public NpcScheduleState(EntityId entityId, DailySchedule schedule) {
        this.entityId = entityId;
        this.schedule = schedule;
        for (NeedType type : NeedType.values()) {
            needs.put(type, NeedState.defaultFor(type));
        }
    }

    public Optional<ScheduledActivity> currentActivity(int gameHour) {
        return schedule.activityAt(gameHour);
    }

    public Map<NeedType, NeedState> needs() {
        return Map.copyOf(needs);
    }

    public Optional<ScheduleInterruption> currentInterruption() {
        return Optional.ofNullable(interruption);
    }

    public void interrupt(ScheduleInterruption interruption) {
        this.interruption = interruption;
    }

    public void clearInterruption() {
        this.interruption = null;
    }

    public boolean isInterrupted() {
        return interruption != null;
    }

    public void tickNeeds(ScheduledActivity currentActivity) {
        NeedType met = null;
        if (!isInterrupted() && currentActivity != null) {
            met = currentActivity.satisfies();
        }

        for (Map.Entry<NeedType, NeedState> entry : needs.entrySet()) {
            boolean isMet = met != null && met == entry.getKey();
            needs.put(entry.getKey(), entry.getValue().tick(isMet));
        }
    }

    public NeedType dominantNeed() {
        return needs.values().stream()
            .max(java.util.Comparator.comparingDouble(NeedState::urgency))
            .map(NeedState::type)
            .orElse(NeedType.SAFETY);
    }

    public EntityId entityId() {
        return entityId;
    }
}
