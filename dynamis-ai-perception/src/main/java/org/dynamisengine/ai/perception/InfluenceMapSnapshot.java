package org.dynamisengine.ai.perception;

import org.dynamisengine.ai.core.Location;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Immutable influence-map view consumed by planning systems. */
public record InfluenceMapSnapshot(
    Map<InfluenceLayer, float[]> layers,
    int width,
    int height,
    float cellSizeMetres,
    long tick
) {
    public InfluenceMapSnapshot {
        Objects.requireNonNull(layers);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        if (cellSizeMetres <= 0f) {
            throw new IllegalArgumentException("cellSizeMetres must be > 0");
        }

        EnumMap<InfluenceLayer, float[]> copy = new EnumMap<>(InfluenceLayer.class);
        for (InfluenceLayer layer : InfluenceLayer.values()) {
            float[] src = layers.get(layer);
            if (src == null) {
                src = new float[width * height];
            }
            if (src.length != width * height) {
                throw new IllegalArgumentException("Layer " + layer + " has wrong size " + src.length);
            }
            copy.put(layer, Arrays.copyOf(src, src.length));
        }
        layers = Map.copyOf(copy);
    }

    public float sample(InfluenceLayer layer, Location worldPos) {
        Objects.requireNonNull(layer);
        Objects.requireNonNull(worldPos);
        float[] data = layers.get(layer);
        if (data == null || data.length == 0) {
            return 0f;
        }

        float fx = (worldPos.x() / cellSizeMetres) - 0.5f;
        float fz = (worldPos.z() / cellSizeMetres) - 0.5f;

        int x0 = clampIndex((int) Math.floor(fx), width);
        int z0 = clampIndex((int) Math.floor(fz), height);
        int x1 = clampIndex(x0 + 1, width);
        int z1 = clampIndex(z0 + 1, height);

        float tx = clamp01(fx - x0);
        float tz = clamp01(fz - z0);

        float v00 = valueAt(data, x0, z0, width, height);
        float v10 = valueAt(data, x1, z0, width, height);
        float v01 = valueAt(data, x0, z1, width, height);
        float v11 = valueAt(data, x1, z1, width, height);

        float i0 = lerp(v00, v10, tx);
        float i1 = lerp(v01, v11, tx);
        return lerp(i0, i1, tz);
    }

    public Location highestCell(InfluenceLayer layer) {
        Objects.requireNonNull(layer);
        float[] data = layers.get(layer);
        if (data == null || data.length == 0) {
            return new Location(0f, 0f, 0f);
        }

        int bestIdx = 0;
        float best = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < data.length; i++) {
            if (data[i] > best) {
                best = data[i];
                bestIdx = i;
            }
        }
        int x = bestIdx % width;
        int z = bestIdx / width;
        return new Location((x + 0.5f) * cellSizeMetres, 0f, (z + 0.5f) * cellSizeMetres);
    }

    public Location lowestCellNear(InfluenceLayer layer, Location centre, float radiusMetres) {
        Objects.requireNonNull(layer);
        Objects.requireNonNull(centre);

        float[] data = layers.get(layer);
        if (data == null || data.length == 0) {
            return new Location(0f, 0f, 0f);
        }

        int centreX = clampIndex(Math.round((centre.x() / cellSizeMetres) - 0.5f), width);
        int centreZ = clampIndex(Math.round((centre.z() / cellSizeMetres) - 0.5f), height);

        int bestIdx = centreZ * width + centreX;
        float best = data[bestIdx];

        float radiusSq = radiusMetres * radiusMetres;
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                float cx = (x + 0.5f) * cellSizeMetres;
                float cz = (z + 0.5f) * cellSizeMetres;
                float dx = cx - centre.x();
                float dz = cz - centre.z();
                if ((dx * dx + dz * dz) > radiusSq) {
                    continue;
                }
                float value = data[z * width + x];
                if (value < best) {
                    best = value;
                    bestIdx = z * width + x;
                }
            }
        }

        int bx = bestIdx % width;
        int bz = bestIdx / width;
        return new Location((bx + 0.5f) * cellSizeMetres, 0f, (bz + 0.5f) * cellSizeMetres);
    }

    private static float valueAt(float[] data, int x, int z, int width, int height) {
        if (x < 0 || z < 0 || x >= width || z >= height) {
            return 0f;
        }
        return data[z * width + x];
    }

    private static int clampIndex(int idx, int max) {
        if (idx < 0) {
            return 0;
        }
        if (idx >= max) {
            return max - 1;
        }
        return idx;
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

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
