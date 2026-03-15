package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.WorldSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AIInspector {

    private final SnapshotStore snapshotStore;
    private final DebugSnapshotHistory debugHistory;
    private final Set<ReplaySession> sessions = ConcurrentHashMap.newKeySet();

    public AIInspector() {
        this(SnapshotStore.DEFAULT_CAPACITY, DebugSnapshotHistory.DEFAULT_HISTORY_TICKS);
    }

    public AIInspector(int snapshotCapacity, int debugHistoryTicks) {
        this.snapshotStore = new SnapshotStore(snapshotCapacity);
        this.debugHistory = new DebugSnapshotHistory(debugHistoryTicks);
    }

    public void record(WorldSnapshot snapshot, Map<EntityId, NpcDebugSnapshot> debugSnapshots) {
        if (snapshot == null) {
            return;
        }
        snapshotStore.record(snapshot);
        if (debugSnapshots != null) {
            debugSnapshots.values().forEach(debugHistory::record);
        }
    }

    public ReplaySession openReplay() {
        ReplaySession session = new ReplaySession(snapshotStore, debugHistory);
        sessions.add(session);
        return session;
    }

    public void closeReplay(ReplaySession session) {
        if (session != null) {
            sessions.remove(session);
        }
    }

    public String timeline(int lastNTicks) {
        List<WorldSnapshotRecord> recent = snapshotStore.recent(lastNTicks);
        if (recent.isEmpty()) {
            return "<empty timeline>";
        }
        List<String> lines = new ArrayList<>();
        for (int i = recent.size() - 1; i >= 0; i--) {
            WorldSnapshotRecord r = recent.get(i);
            int debugCount = debugHistory.snapshotsAtTick(r.tick()).size();
            lines.add(String.format("tick=%d entities=%d seed=%d debug=%d",
                r.tick(), r.entities().size(), r.deterministicSeed(), debugCount));
        }
        return String.join("\n", lines);
    }

    public String exportSnapshots() {
        return snapshotStore.exportJsonLines();
    }

    public void importSnapshots(String jsonLines) {
        snapshotStore.importJsonLines(jsonLines);
    }

    public SnapshotStore snapshotStore() {
        return snapshotStore;
    }

    public DebugSnapshotHistory debugHistory() {
        return debugHistory;
    }

    public int activeReplaySessions() {
        return sessions.size();
    }
}
