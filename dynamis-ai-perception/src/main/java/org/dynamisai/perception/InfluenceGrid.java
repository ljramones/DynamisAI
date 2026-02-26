package org.dynamisai.perception;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import org.dynamisai.core.Location;

import java.util.Arrays;
import java.util.Objects;

/** One influence layer stored as a flat grid with SIMD-accelerated bulk operations. */
public final class InfluenceGrid {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private final int width;
    private final int height;
    private final float cellSizeMetres;
    private final float minValue;
    private final float maxValue;
    private final float[] cells;

    public InfluenceGrid(int width, int height, float cellSizeMetres) {
        this(width, height, cellSizeMetres, 0f, 1f);
    }

    InfluenceGrid(int width, int height, float cellSizeMetres, float minValue, float maxValue) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        if (cellSizeMetres <= 0f) {
            throw new IllegalArgumentException("cellSizeMetres must be > 0");
        }
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue must be <= maxValue");
        }
        this.width = width;
        this.height = height;
        this.cellSizeMetres = cellSizeMetres;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cells = new float[width * height];
    }

    public void set(int x, int z, float value) {
        if (!inBounds(x, z)) {
            return;
        }
        cells[index(x, z)] = clamp(value);
    }

    public float get(int x, int z) {
        if (!inBounds(x, z)) {
            return 0f;
        }
        return cells[index(x, z)];
    }

    public float sampleAt(Location worldPos) {
        Objects.requireNonNull(worldPos);

        float fx = (worldPos.x() / cellSizeMetres) - 0.5f;
        float fz = (worldPos.z() / cellSizeMetres) - 0.5f;

        int x0 = clampIndex((int) Math.floor(fx), width);
        int z0 = clampIndex((int) Math.floor(fz), height);
        int x1 = clampIndex(x0 + 1, width);
        int z1 = clampIndex(z0 + 1, height);

        float tx = clamp01(fx - x0);
        float tz = clamp01(fz - z0);

        float v00 = get(x0, z0);
        float v10 = get(x1, z0);
        float v01 = get(x0, z1);
        float v11 = get(x1, z1);

        float i0 = lerp(v00, v10, tx);
        float i1 = lerp(v01, v11, tx);
        return lerp(i0, i1, tz);
    }

    public Location cellCentre(int x, int z) {
        return new Location((x + 0.5f) * cellSizeMetres, 0f, (z + 0.5f) * cellSizeMetres);
    }

    public int[] worldToCell(Location worldPos) {
        Objects.requireNonNull(worldPos);
        int x = clampIndex(Math.round((worldPos.x() / cellSizeMetres) - 0.5f), width);
        int z = clampIndex(Math.round((worldPos.z() / cellSizeMetres) - 0.5f), height);
        return new int[]{x, z};
    }

    public void decayAll(float decayFactor) {
        FloatVector factor = FloatVector.broadcast(SPECIES, decayFactor);
        FloatVector min = FloatVector.broadcast(SPECIES, minValue);
        FloatVector max = FloatVector.broadcast(SPECIES, maxValue);

        int i = 0;
        int upper = SPECIES.loopBound(cells.length);
        for (; i < upper; i += SPECIES.length()) {
            FloatVector v = FloatVector.fromArray(SPECIES, cells, i)
                .mul(factor)
                .max(min)
                .min(max);
            v.intoArray(cells, i);
        }
        for (; i < cells.length; i++) {
            cells[i] = clamp(cells[i] * decayFactor);
        }
    }

    public void addRadialInfluence(Location centre, float radius, float peakValue) {
        Objects.requireNonNull(centre);
        if (radius <= 0f || peakValue == 0f) {
            return;
        }

        int[] c = worldToCell(centre);
        int centreX = c[0];
        int centreZ = c[1];
        int radiusCells = (int) Math.ceil(radius / cellSizeMetres);

        float[] xWorld = new float[SPECIES.length()];
        float[] zWorld = new float[SPECIES.length()];

        FloatVector centreXv = FloatVector.broadcast(SPECIES, centre.x());
        FloatVector centreZv = FloatVector.broadcast(SPECIES, centre.z());
        FloatVector radiusV = FloatVector.broadcast(SPECIES, radius);
        FloatVector peakV = FloatVector.broadcast(SPECIES, peakValue);
        FloatVector oneV = FloatVector.broadcast(SPECIES, 1f);
        FloatVector zeroV = FloatVector.broadcast(SPECIES, 0f);
        FloatVector minV = FloatVector.broadcast(SPECIES, minValue);
        FloatVector maxV = FloatVector.broadcast(SPECIES, maxValue);

        for (int z = Math.max(0, centreZ - radiusCells); z <= Math.min(height - 1, centreZ + radiusCells); z++) {
            int minX = Math.max(0, centreX - radiusCells);
            int maxX = Math.min(width - 1, centreX + radiusCells);

            int x = minX;
            int vecMax = maxX - SPECIES.length() + 1;
            for (; x <= vecMax; x += SPECIES.length()) {
                for (int lane = 0; lane < SPECIES.length(); lane++) {
                    xWorld[lane] = ((x + lane) + 0.5f) * cellSizeMetres;
                    zWorld[lane] = (z + 0.5f) * cellSizeMetres;
                }

                FloatVector xV = FloatVector.fromArray(SPECIES, xWorld, 0);
                FloatVector zV = FloatVector.fromArray(SPECIES, zWorld, 0);
                FloatVector dxV = xV.sub(centreXv);
                FloatVector dzV = zV.sub(centreZv);
                FloatVector distSq = dxV.mul(dxV).add(dzV.mul(dzV));
                FloatVector dist = distSq.lanewise(VectorOperators.SQRT);
                FloatVector norm = oneV.sub(dist.div(radiusV));
                FloatVector falloff = norm.max(zeroV);
                FloatVector delta = falloff.mul(peakV);

                int idx = index(x, z);
                FloatVector current = FloatVector.fromArray(SPECIES, cells, idx);
                current.add(delta).max(minV).min(maxV).intoArray(cells, idx);
            }

            for (; x <= maxX; x++) {
                Location cellPos = cellCentre(x, z);
                float dist = centre.distanceTo(cellPos);
                if (dist > radius) {
                    continue;
                }
                float falloff = 1f - (dist / radius);
                float delta = peakValue * Math.max(0f, falloff);
                int idx = index(x, z);
                cells[idx] = clamp(cells[idx] + delta);
            }
        }
    }

    public void propagate(float selfWeight, float neighbourWeight) {
        float[] prev = Arrays.copyOf(cells, cells.length);

        FloatVector selfW = FloatVector.broadcast(SPECIES, selfWeight);
        FloatVector neighbourW = FloatVector.broadcast(SPECIES, neighbourWeight);
        FloatVector minV = FloatVector.broadcast(SPECIES, minValue);
        FloatVector maxV = FloatVector.broadcast(SPECIES, maxValue);

        for (int z = 1; z < height - 1; z++) {
            int rowStart = z * width;
            int x = 1;
            int vecEnd = (width - 1) - SPECIES.length();
            for (; x <= vecEnd; x += SPECIES.length()) {
                int idx = rowStart + x;
                FloatVector self = FloatVector.fromArray(SPECIES, prev, idx);
                FloatVector left = FloatVector.fromArray(SPECIES, prev, idx - 1);
                FloatVector right = FloatVector.fromArray(SPECIES, prev, idx + 1);
                FloatVector up = FloatVector.fromArray(SPECIES, prev, idx - width);
                FloatVector down = FloatVector.fromArray(SPECIES, prev, idx + width);

                FloatVector blended = self.mul(selfW)
                    .add(left.add(right).add(up).add(down).mul(neighbourW))
                    .max(minV)
                    .min(maxV);
                blended.intoArray(cells, idx);
            }

            for (; x < width - 1; x++) {
                int idx = rowStart + x;
                float blended = prev[idx] * selfWeight
                    + (prev[idx - 1] + prev[idx + 1] + prev[idx - width] + prev[idx + width])
                    * neighbourWeight;
                cells[idx] = clamp(blended);
            }
        }

        for (int x = 0; x < width; x++) {
            cells[index(x, 0)] = clamp(edgeBlend(prev, x, 0, selfWeight, neighbourWeight));
            cells[index(x, height - 1)] = clamp(edgeBlend(prev, x, height - 1, selfWeight, neighbourWeight));
        }
        for (int z = 1; z < height - 1; z++) {
            cells[index(0, z)] = clamp(edgeBlend(prev, 0, z, selfWeight, neighbourWeight));
            cells[index(width - 1, z)] = clamp(edgeBlend(prev, width - 1, z, selfWeight, neighbourWeight));
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float cellSizeMetres() {
        return cellSizeMetres;
    }

    public float[] rawSnapshot() {
        return Arrays.copyOf(cells, cells.length);
    }

    private float edgeBlend(float[] src, int x, int z, float selfWeight, float neighbourWeight) {
        int idx = index(x, z);
        float self = src[idx];
        float left = src[index(Math.max(0, x - 1), z)];
        float right = src[index(Math.min(width - 1, x + 1), z)];
        float up = src[index(x, Math.max(0, z - 1))];
        float down = src[index(x, Math.min(height - 1, z + 1))];
        return self * selfWeight + (left + right + up + down) * neighbourWeight;
    }

    private boolean inBounds(int x, int z) {
        return x >= 0 && x < width && z >= 0 && z < height;
    }

    private int index(int x, int z) {
        return z * width + x;
    }

    private float clamp(float value) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
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

    private static int clampIndex(int idx, int max) {
        if (idx < 0) {
            return 0;
        }
        if (idx >= max) {
            return max - 1;
        }
        return idx;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
