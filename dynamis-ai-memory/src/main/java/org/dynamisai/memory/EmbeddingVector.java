package org.dynamisai.memory;

import java.util.Arrays;

/**
 * A dense float embedding vector with fixed dimensionality.
 *
 * Immutable. Dimensionality is set at construction and never changes.
 * Used as the semantic key for VectorMemoryStore similarity queries.
 */
public final class EmbeddingVector {

    private final float[] values;

    public EmbeddingVector(float[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("EmbeddingVector must be non-empty");
        }
        this.values = Arrays.copyOf(values, values.length);
    }

    /** Dimension count. */
    public int dim() {
        return values.length;
    }

    /** Raw float at index i — zero-allocation. */
    public float get(int i) {
        return values[i];
    }

    /** Defensive copy for callers that need an array. */
    public float[] toArray() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Scalar cosine similarity — used as fallback and in tests.
     * Returns value in [-1, 1]. Returns 0 if either vector has zero magnitude.
     */
    public float cosineSimilarity(EmbeddingVector other) {
        if (other.dim() != dim()) {
            throw new IllegalArgumentException(
                "Dimension mismatch: " + dim() + " vs " + other.dim());
        }
        float dot = 0f;
        float magA = 0f;
        float magB = 0f;
        for (int i = 0; i < values.length; i++) {
            dot += values[i] * other.values[i];
            magA += values[i] * values[i];
            magB += other.values[i] * other.values[i];
        }
        if (magA < 1e-12f || magB < 1e-12f) {
            return 0f;
        }
        return dot / (float) (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /**
     * L2-normalize this vector — returns a new EmbeddingVector.
     * Pre-normalizing stored vectors reduces cosineSimilarity to a dot product.
     */
    public EmbeddingVector normalize() {
        float mag = 0f;
        for (float v : values) {
            mag += v * v;
        }
        if (mag < 1e-12f) {
            return this;
        }
        float invMag = (float) (1.0 / Math.sqrt(mag));
        float[] norm = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            norm[i] = values[i] * invMag;
        }
        return new EmbeddingVector(norm);
    }

    /** Two EmbeddingVectors are equal if all elements match within tolerance. */
    public boolean approximatelyEquals(EmbeddingVector other, float tolerance) {
        if (other.dim() != dim()) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (Math.abs(values[i] - other.values[i]) > tolerance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "EmbeddingVector[dim=" + dim() + "]";
    }
}
