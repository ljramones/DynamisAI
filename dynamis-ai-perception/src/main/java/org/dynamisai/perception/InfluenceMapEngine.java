package org.dynamisai.perception;

import io.vavr.collection.HashMap;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldSnapshot;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tactical-rate multi-layer influence-map engine.
 * Owns mutable grids and produces immutable snapshots for planning reads.
 */
public final class InfluenceMapEngine {

    private final int gridWidth;
    private final int gridHeight;
    private final float cellSizeMetres;
    private final Location gridOrigin;
    private final EnumMap<InfluenceLayer, InfluenceGrid> grids = new EnumMap<>(InfluenceLayer.class);

    public InfluenceMapEngine(int gridWidth, int gridHeight,
                              float cellSizeMetres, Location gridOrigin) {
        if (gridWidth <= 0 || gridHeight <= 0) {
            throw new IllegalArgumentException("grid dimensions must be > 0");
        }
        if (cellSizeMetres <= 0f) {
            throw new IllegalArgumentException("cellSizeMetres must be > 0");
        }
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.cellSizeMetres = cellSizeMetres;
        this.gridOrigin = Objects.requireNonNull(gridOrigin);

        grids.put(InfluenceLayer.THREAT, new InfluenceGrid(gridWidth, gridHeight, cellSizeMetres));
        grids.put(InfluenceLayer.COVER, new InfluenceGrid(gridWidth, gridHeight, cellSizeMetres));
        grids.put(InfluenceLayer.SOUND, new InfluenceGrid(gridWidth, gridHeight, cellSizeMetres));
        grids.put(InfluenceLayer.TERRITORIAL,
            new InfluenceGrid(gridWidth, gridHeight, cellSizeMetres, -1f, 1f));
        grids.put(InfluenceLayer.MOVEMENT, new InfluenceGrid(gridWidth, gridHeight, cellSizeMetres));
    }

    public void update(WorldSnapshot snapshot, List<SoundEvent> recentSoundEvents) {
        Objects.requireNonNull(snapshot);
        List<SoundEvent> sounds = recentSoundEvents == null ? List.of() : recentSoundEvents;

        InfluenceGrid threat = grids.get(InfluenceLayer.THREAT);
        InfluenceGrid sound = grids.get(InfluenceLayer.SOUND);
        InfluenceGrid territorial = grids.get(InfluenceLayer.TERRITORIAL);

        threat.decayAll(0.85f);
        sound.decayAll(0.7f);
        territorial.decayAll(0.95f);

        for (Map.Entry<EntityId, EntityState> entry : toJavaMap(snapshot.entities()).entrySet()) {
            EntityState state = entry.getValue();
            Location localPos = toLocal(state.position());

            ThreatLevel level = threatOf(state);
            if (level == ThreatLevel.CRITICAL) {
                threat.addRadialInfluence(localPos, 20f, 1.0f);
            } else if (level == ThreatLevel.HIGH) {
                threat.addRadialInfluence(localPos, 20f, 0.6f);
            }

            float territorialPeak = territorialPeak(state, level);
            if (territorialPeak != 0f) {
                territorial.addRadialInfluence(localPos, 10f, territorialPeak);
            }
        }

        for (SoundEvent event : sounds) {
            float intensity = clamp01(event.intensity());
            if (intensity <= 0f) {
                continue;
            }
            sound.addRadialInfluence(toLocal(event.position()), intensity * 30f, intensity);
        }
        sound.propagate(0.6f, 0.1f);
    }

    public InfluenceMapSnapshot snapshot(long tick) {
        EnumMap<InfluenceLayer, float[]> copied = new EnumMap<>(InfluenceLayer.class);
        for (InfluenceLayer layer : InfluenceLayer.values()) {
            copied.put(layer, grid(layer).rawSnapshot());
        }
        return new InfluenceMapSnapshot(copied, gridWidth, gridHeight, cellSizeMetres, tick);
    }

    public InfluenceGrid grid(InfluenceLayer layer) {
        InfluenceGrid grid = grids.get(layer);
        if (grid == null) {
            throw new IllegalArgumentException("Unknown layer: " + layer);
        }
        return grid;
    }

    public void addCoverAt(Location position, float quality) {
        grids.get(InfluenceLayer.COVER).addRadialInfluence(toLocal(position), cellSizeMetres, clamp01(quality));
    }

    public void addMovementCost(Location position, float cost) {
        grids.get(InfluenceLayer.MOVEMENT).addRadialInfluence(toLocal(position), cellSizeMetres, clamp01(cost));
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityId, EntityState> toJavaMap(HashMap<EntityId, EntityState> entities) {
        return entities.toJavaMap();
    }

    private static ThreatLevel threatOf(EntityState state) {
        Object value = state.properties().get("threatLevel");
        if (value instanceof ThreatLevel level) {
            return level;
        }
        return ThreatLevel.NONE;
    }

    private static float territorialPeak(EntityState state, ThreatLevel level) {
        Object explicit = state.properties().get("territorialInfluence");
        if (explicit instanceof Number n) {
            return clampSigned(n.floatValue());
        }

        Object faction = state.properties().get("faction");
        if (faction instanceof String f) {
            if (f.equalsIgnoreCase("friendly")) {
                return 0.3f;
            }
            if (f.equalsIgnoreCase("hostile")) {
                return -0.3f;
            }
        }

        if (level == ThreatLevel.CRITICAL) {
            return -0.3f;
        }
        return 0f;
    }

    private Location toLocal(Location world) {
        return new Location(world.x() - gridOrigin.x(), world.y(), world.z() - gridOrigin.z());
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }

    private static float clampSigned(float value) {
        if (value < -1f) {
            return -1f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}
