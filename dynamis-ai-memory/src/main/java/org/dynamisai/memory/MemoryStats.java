package org.dynamisai.memory;

import org.dynamisai.core.EntityId;

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
