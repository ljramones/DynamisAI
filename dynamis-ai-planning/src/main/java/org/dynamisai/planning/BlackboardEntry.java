package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;

import java.util.Objects;

public record BlackboardEntry(
    String key,
    Object value,
    EntityId author,
    long writtenAtTick,
    PropagationChannel channel,
    float propagationSpeed
) {
    public static final float LINE_OF_SIGHT_THRESHOLD = 15f;

    public BlackboardEntry {
        Objects.requireNonNull(key);
        Objects.requireNonNull(author);
        Objects.requireNonNull(channel);
        if (channel == PropagationChannel.RUNNER && propagationSpeed <= 0f) {
            throw new IllegalArgumentException("RUNNER propagationSpeed must be > 0");
        }
    }

    public boolean isAvailableTo(EntityId reader, Location readerPos,
                                  Location authorPos, long currentTick) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(readerPos);
        Objects.requireNonNull(authorPos);

        return switch (channel) {
            case RADIO -> true;
            case LINE_OF_SIGHT -> readerPos.distanceTo(authorPos) < LINE_OF_SIGHT_THRESHOLD;
            case RUNNER -> {
                float distance = readerPos.distanceTo(authorPos);
                float ticksNeeded = distance / propagationSpeed;
                yield (currentTick - writtenAtTick) >= ticksNeeded;
            }
        };
    }
}
