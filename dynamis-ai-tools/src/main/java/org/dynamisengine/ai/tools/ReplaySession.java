package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ReplaySession {

    private final SnapshotStore store;
    private final DebugSnapshotHistory debugHistory;
    private long currentTick = -1L;

    public ReplaySession(SnapshotStore store, DebugSnapshotHistory debugHistory) {
        this.store = store;
        this.debugHistory = debugHistory;
    }

    public synchronized Optional<WorldSnapshotRecord> seekTo(long targetTick) {
        Optional<WorldSnapshotRecord> found = store.floor(targetTick);
        if (found.isEmpty()) {
            found = store.oldestTick() >= 0 ? store.atTick(store.oldestTick()) : Optional.empty();
        }
        found.ifPresent(r -> currentTick = r.tick());
        return found;
    }

    public synchronized Optional<WorldSnapshotRecord> stepForward() {
        if (currentTick < 0) {
            return Optional.empty();
        }
        Optional<WorldSnapshotRecord> next = store.higher(currentTick);
        next.ifPresent(r -> currentTick = r.tick());
        return next;
    }

    public synchronized Optional<WorldSnapshotRecord> stepBackward() {
        if (currentTick < 0) {
            return Optional.empty();
        }
        Optional<WorldSnapshotRecord> prev = store.lower(currentTick);
        prev.ifPresent(r -> currentTick = r.tick());
        return prev;
    }

    public synchronized long currentTick() {
        return currentTick;
    }

    public synchronized boolean isReplaying() {
        return currentTick >= 0;
    }

    public synchronized Map<EntityId, NpcDebugSnapshot> debugSnapshotsAtCurrentTick() {
        if (currentTick < 0) {
            return Map.of();
        }
        return debugHistory.snapshotsAtTick(currentTick);
    }

    public synchronized void exitReplay() {
        currentTick = -1L;
    }
}
