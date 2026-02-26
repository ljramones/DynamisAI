package org.dynamisai.memory;

/**
 * Deterministic hash-based encoder for tests and fallback behavior.
 */
public final class MockSentenceEncoder implements SentenceEncoder {

    public static final int DIM = 384;

    @Override
    public EmbeddingVector encode(String text) {
        return hashProject(text, DIM);
    }

    @Override
    public int dim() {
        return DIM;
    }

    static EmbeddingVector hashProject(String text, int dim) {
        float[] vec = new float[dim];
        if (text == null || text.isEmpty()) {
            return new EmbeddingVector(vec);
        }
        for (int i = 0; i < text.length(); i++) {
            int bucket = Math.abs(text.charAt(i) * 31 + i) % dim;
            vec[bucket] += 1.0f;
        }
        return new EmbeddingVector(vec).normalize();
    }
}
