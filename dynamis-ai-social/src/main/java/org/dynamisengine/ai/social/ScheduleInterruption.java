package org.dynamisengine.ai.social;

public record ScheduleInterruption(
    String reason,
    int minimumPriorityToOverride,
    long startedAtTick,
    long expiresAtTick
) {
    public boolean isExpired(long currentTick) {
        return expiresAtTick > 0 && currentTick >= expiresAtTick;
    }
}
