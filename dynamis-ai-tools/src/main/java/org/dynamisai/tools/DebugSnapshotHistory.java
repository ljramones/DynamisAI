package org.dynamisai.tools;

import org.dynamis.core.entity.EntityId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugSnapshotHistory {

    public static final int DEFAULT_HISTORY_TICKS = 600;

    private final int maxHistoryTicks;
    private final Map<EntityId, Deque<NpcDebugSnapshot>> history = new ConcurrentHashMap<>();

    public DebugSnapshotHistory(int maxHistoryTicks) {
        this.maxHistoryTicks = Math.max(1, maxHistoryTicks);
    }

    public void record(NpcDebugSnapshot snapshot) {
        Deque<NpcDebugSnapshot> deque = history.computeIfAbsent(snapshot.agent(), ignored -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(snapshot);
            while (deque.size() > maxHistoryTicks) {
                deque.removeFirst();
            }
        }
    }

    public List<NpcDebugSnapshot> historyFor(EntityId agent) {
        Deque<NpcDebugSnapshot> deque = history.get(agent);
        if (deque == null) {
            return List.of();
        }
        synchronized (deque) {
            return List.copyOf(deque);
        }
    }

    public Optional<NpcDebugSnapshot> atTick(EntityId agent, long tick) {
        return historyFor(agent).stream()
            .filter(s -> s.tick() == tick)
            .findFirst();
    }

    public Map<EntityId, NpcDebugSnapshot> snapshotsAtTick(long tick) {
        Map<EntityId, NpcDebugSnapshot> out = new ConcurrentHashMap<>();
        history.forEach((id, deque) -> {
            synchronized (deque) {
                deque.stream()
                    .filter(s -> s.tick() == tick)
                    .findFirst()
                    .ifPresent(s -> out.put(id, s));
            }
        });
        return Map.copyOf(out);
    }

    public void pruneOlderThan(long tick) {
        for (Deque<NpcDebugSnapshot> deque : history.values()) {
            synchronized (deque) {
                while (!deque.isEmpty() && deque.peekFirst().tick() < tick) {
                    deque.removeFirst();
                }
            }
        }
    }

    public int snapshotCount(EntityId agent) {
        Deque<NpcDebugSnapshot> deque = history.get(agent);
        if (deque == null) {
            return 0;
        }
        synchronized (deque) {
            return deque.size();
        }
    }
}
