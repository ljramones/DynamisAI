package org.dynamisengine.ai.planning;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SquadBlackboardRegistry {

    private final ConcurrentMap<String, SquadBlackboard> squads = new ConcurrentHashMap<>();

    public SquadBlackboard getOrCreate(String squadId) {
        return squads.computeIfAbsent(squadId, SquadBlackboard::new);
    }

    public Optional<SquadBlackboard> get(String squadId) {
        return Optional.ofNullable(squads.get(squadId));
    }

    public void disband(String squadId) {
        squads.remove(squadId);
    }

    public void pruneAll(long currentTick, long maxAgeTicks) {
        squads.values().forEach(bb -> bb.pruneStale(currentTick, maxAgeTicks));
    }

    public int squadCount() {
        return squads.size();
    }
}
