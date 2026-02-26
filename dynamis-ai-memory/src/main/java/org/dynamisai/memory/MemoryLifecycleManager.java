package org.dynamisai.memory;

import org.dynamisai.core.EntityId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemoryLifecycleManager {

    /** Add a new raw event to an NPC's memory. */
    void addRawEvent(MemoryRecord record);

    /**
     * Run one consolidation tick for an NPC.
     * Promotes eligible records, prunes over-budget stages.
     * Called from deliberative tick (~1 Hz) — never from reactive layer.
     */
    void consolidate(EntityId owner);

    /** Retrieve all non-pruned records for an NPC, sorted by importance descending. */
    List<MemoryRecord> getMemories(EntityId owner);

    /** Retrieve memories at a specific lifecycle stage. */
    List<MemoryRecord> getMemoriesAtStage(EntityId owner, MemoryLifecycleStage stage);

    /** Retrieve a specific record by ID. */
    Optional<MemoryRecord> getById(UUID id);

    /** Force-prune all records for an NPC — called on despawn. */
    void purgeAll(EntityId owner);

    /** Current memory count per stage for an NPC — for Inspector tooling. */
    MemoryStats getStats(EntityId owner);
}
