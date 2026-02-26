package org.dynamisai.social;

import org.dynamisai.core.Location;

import java.util.Objects;

public record ScheduledActivity(
    String name,
    int startHour,
    int endHour,
    Location location,
    NeedType satisfies,
    int priority
) {
    public ScheduledActivity {
        Objects.requireNonNull(name);
        Objects.requireNonNull(location);
        Objects.requireNonNull(satisfies);
        if (startHour < 0 || startHour > 23) {
            throw new IllegalArgumentException("startHour must be [0,23]");
        }
        if (endHour <= startHour || endHour > 24) {
            throw new IllegalArgumentException("endHour must be > startHour and <= 24");
        }
    }
}
