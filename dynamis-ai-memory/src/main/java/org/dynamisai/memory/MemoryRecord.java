package org.dynamisai.memory;

import org.dynamisai.core.EntityId;

import java.time.Instant;
import java.util.UUID;

/**
 * A single NPC memory record with full lifecycle metadata.
 * Immutable — transitions produce new records, never mutate existing ones.
 */
public record MemoryRecord(
    UUID id,
    EntityId owner,
    MemoryLifecycleStage stage,
    float importanceScore,
    Instant created,
    Instant lastAccessed,
    String summary,
    Object payload
) {
    public MemoryRecord {
        if (importanceScore < 0f || importanceScore > 1f) {
            throw new IllegalArgumentException("importanceScore must be in [0,1]");
        }
    }

    /** Transition to the next stage — produces a new record. */
    public MemoryRecord withStage(MemoryLifecycleStage newStage) {
        return new MemoryRecord(id, owner, newStage, importanceScore,
            created, Instant.now(), summary, payload);
    }

    /** Update importance score — produces a new record. */
    public MemoryRecord withImportance(float newScore) {
        return new MemoryRecord(id, owner, stage, newScore,
            created, lastAccessed, summary, payload);
    }

    /** Mark as accessed — updates lastAccessed timestamp. */
    public MemoryRecord accessed() {
        return new MemoryRecord(id, owner, stage, importanceScore,
            created, Instant.now(), summary, payload);
    }

    /** Factory — create a new raw event record. */
    public static MemoryRecord rawEvent(EntityId owner, String summary,
                                        Object payload, float importance) {
        Instant now = Instant.now();
        return new MemoryRecord(UUID.randomUUID(), owner,
            MemoryLifecycleStage.RAW_EVENT, importance, now, now, summary, payload);
    }

    /** Generic factory used by off-heap store tests. */
    public static MemoryRecord create(EntityId owner, String summary,
                                      Object payload, float importance) {
        return rawEvent(owner, summary, payload, importance);
    }
}
