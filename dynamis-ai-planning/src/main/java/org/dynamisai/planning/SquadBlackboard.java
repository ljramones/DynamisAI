package org.dynamisai.planning;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SquadBlackboard {

    private static final float DEFAULT_RUNNER_SPEED = 5f;

    private final String squadId;
    private final ConcurrentMap<String, BlackboardEntry> entries = new ConcurrentHashMap<>();

    public SquadBlackboard(String squadId) {
        this.squadId = Objects.requireNonNull(squadId);
    }

    public void write(String key, Object value, EntityId author,
                      PropagationChannel channel, long tick) {
        float speed = channel == PropagationChannel.RUNNER ? DEFAULT_RUNNER_SPEED : 0f;
        entries.put(key, new BlackboardEntry(key, value, author, tick, channel, speed));
    }

    public void writeRadio(String key, Object value, EntityId author, long tick) {
        write(key, value, author, PropagationChannel.RADIO, tick);
    }

    public Optional<Object> read(String key, EntityId reader,
                                 Location readerPos, Location authorPos,
                                 long currentTick) {
        BlackboardEntry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (!entry.isAvailableTo(reader, readerPos, authorPos, currentTick)) {
            return Optional.empty();
        }
        return Optional.ofNullable(entry.value());
    }

    public Optional<Object> readDirect(String key) {
        BlackboardEntry entry = entries.get(key);
        return entry == null ? Optional.empty() : Optional.ofNullable(entry.value());
    }

    public void retract(String key) {
        entries.remove(key);
    }

    public Set<String> keys() {
        return Set.copyOf(entries.keySet());
    }

    public void pruneStale(long currentTick, long maxAgeTicks) {
        entries.entrySet().removeIf(e -> (currentTick - e.getValue().writtenAtTick()) > maxAgeTicks);
    }

    public String squadId() {
        return squadId;
    }

    public int entryCount() {
        return entries.size();
    }

    public Map<String, BlackboardEntry> snapshotEntries() {
        return Map.copyOf(entries);
    }
}
