package org.dynamisengine.ai.memory;

import org.dynamisengine.core.entity.EntityId;

/**
 * Per-NPC memory statistics — fed to AffectiveStateDebugger and AIInspector.
 */
public record MemoryStats(
    EntityId owner,
    int rawEventCount,
    int shortTermCount,
    int consolidatedCount,
    int archivedCount,
    int totalCount
) {}
