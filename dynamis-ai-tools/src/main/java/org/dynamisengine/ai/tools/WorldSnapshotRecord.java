package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.WorldSnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

public record WorldSnapshotRecord(
    long tick,
    long deterministicSeed,
    Map<String, Map<String, Object>> entities,
    long recordedAtWallTime
) {
    public WorldSnapshotRecord {
        entities = entities == null ? Map.of() : deepCopy(entities);
    }

    public static WorldSnapshotRecord from(WorldSnapshot snapshot) {
        Map<String, Map<String, Object>> converted = new LinkedHashMap<>();
        snapshot.entities().forEach(entry -> {
            EntityId id = entry._1;
            EntityState state = entry._2;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("position", state.position());
            if (state.properties() != null) {
                values.putAll(state.properties());
            }
            converted.put(id.toString(), Map.copyOf(values));
        });
        return new WorldSnapshotRecord(
            snapshot.tick(),
            snapshot.deterministicSeed(),
            converted,
            System.currentTimeMillis()
        );
    }

    public String summary() {
        return String.format("tick=%d entities=%d seed=%d", tick, entities.size(), deterministicSeed);
    }

    private static Map<String, Map<String, Object>> deepCopy(Map<String, Map<String, Object>> source) {
        Map<String, Map<String, Object>> copy = new LinkedHashMap<>();
        source.forEach((k, v) -> copy.put(k, v == null ? Map.of() : Map.copyOf(v)));
        return Map.copyOf(copy);
    }
}
