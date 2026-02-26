package org.dynamisai.social;

import org.dynamisai.core.Location;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DailySchedule {

    private final List<ScheduledActivity> activities;

    public DailySchedule(List<ScheduledActivity> activities) {
        this.activities = activities.stream()
            .sorted(Comparator.comparingInt(ScheduledActivity::startHour))
            .toList();
    }

    public Optional<ScheduledActivity> activityAt(int hour) {
        if (hour < 0 || hour > 23) {
            return Optional.empty();
        }
        return activities.stream()
            .filter(a -> hour >= a.startHour() && hour < a.endHour())
            .findFirst();
    }

    public List<ScheduledActivity> activities() {
        return List.copyOf(activities);
    }

    public static DailySchedule guardPatrol(Location postLocation,
                                            Location restLocation) {
        return new DailySchedule(List.of(
            new ScheduledActivity("night-rest", 0, 6, restLocation, NeedType.SAFETY, 8),
            new ScheduledActivity("day-patrol", 6, 18, postLocation, NeedType.LOYALTY, 10),
            new ScheduledActivity("evening-rest", 18, 24, restLocation, NeedType.SAFETY, 8)
        ));
    }

    public static DailySchedule merchant(Location shopLocation,
                                         Location homeLocation) {
        return new DailySchedule(List.of(
            new ScheduledActivity("home-night", 0, 8, homeLocation, NeedType.SAFETY, 8),
            new ScheduledActivity("open-shop", 8, 12, shopLocation, NeedType.GREED, 9),
            new ScheduledActivity("meal-break", 12, 14, homeLocation, NeedType.SAFETY, 6),
            new ScheduledActivity("open-shop-late", 14, 20, shopLocation, NeedType.GREED, 9),
            new ScheduledActivity("home-evening", 20, 24, homeLocation, NeedType.SAFETY, 8)
        ));
    }
}
