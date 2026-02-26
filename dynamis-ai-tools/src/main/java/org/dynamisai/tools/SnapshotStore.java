package org.dynamisai.tools;

import org.dynamisai.core.WorldSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public final class SnapshotStore {

    public static final int DEFAULT_CAPACITY = 1800;

    private final int capacity;
    private final NavigableMap<Long, WorldSnapshotRecord> records = new TreeMap<>();

    public SnapshotStore(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public synchronized void record(WorldSnapshot snapshot) {
        records.put(snapshot.tick(), WorldSnapshotRecord.from(snapshot));
        trimToCapacity();
    }

    public synchronized Optional<WorldSnapshotRecord> atTick(long tick) {
        return Optional.ofNullable(records.get(tick));
    }

    public synchronized List<WorldSnapshotRecord> recent(int n) {
        if (n <= 0 || records.isEmpty()) {
            return List.of();
        }
        List<WorldSnapshotRecord> out = new ArrayList<>();
        for (WorldSnapshotRecord record : records.descendingMap().values()) {
            out.add(record);
            if (out.size() >= n) {
                break;
            }
        }
        return List.copyOf(out);
    }

    public synchronized long oldestTick() {
        return records.isEmpty() ? -1L : records.firstKey();
    }

    public synchronized long newestTick() {
        return records.isEmpty() ? -1L : records.lastKey();
    }

    public synchronized int size() {
        return records.size();
    }

    public synchronized String exportJsonLines() {
        StringBuilder sb = new StringBuilder();
        for (WorldSnapshotRecord r : records.values()) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append("{\"tick\":").append(r.tick())
                .append(",\"seed\":").append(r.deterministicSeed())
                .append(",\"entityCount\":").append(r.entities().size())
                .append(",\"recordedAt\":").append(r.recordedAtWallTime())
                .append('}');
        }
        return sb.toString();
    }

    public synchronized void importJsonLines(String jsonLines) {
        records.clear();
        if (jsonLines == null || jsonLines.isBlank()) {
            return;
        }
        String[] lines = jsonLines.split("\\R");
        for (String line : lines) {
            parseLine(line).ifPresent(record -> records.put(record.tick(), record));
        }
        trimToCapacity();
    }

    synchronized List<WorldSnapshotRecord> allAscending() {
        return List.copyOf(records.values());
    }

    synchronized Optional<WorldSnapshotRecord> floor(long tick) {
        var entry = records.floorEntry(tick);
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    synchronized Optional<WorldSnapshotRecord> lower(long tick) {
        var entry = records.lowerEntry(tick);
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    synchronized Optional<WorldSnapshotRecord> higher(long tick) {
        var entry = records.higherEntry(tick);
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    private void trimToCapacity() {
        while (records.size() > capacity) {
            records.pollFirstEntry();
        }
    }

    private static Optional<WorldSnapshotRecord> parseLine(String line) {
        if (line == null) {
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        try {
            long tick = extractLong(trimmed, "tick");
            long seed = extractLong(trimmed, "seed");
            long recordedAt = extractLong(trimmed, "recordedAt");
            int entityCount = (int) extractLong(trimmed, "entityCount");
            var entities = new java.util.LinkedHashMap<String, java.util.Map<String, Object>>();
            for (int i = 0; i < Math.max(0, entityCount); i++) {
                entities.put("entity." + i, java.util.Map.of());
            }
            return Optional.of(new WorldSnapshotRecord(tick, seed, entities, recordedAt));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static long extractLong(String json, String key) {
        String token = "\"" + key + "\":";
        int start = json.indexOf(token);
        if (start < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        int idx = start + token.length();
        int end = idx;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (!(Character.isDigit(c) || c == '-')) {
                break;
            }
            end++;
        }
        return Long.parseLong(json.substring(idx, end));
    }
}
