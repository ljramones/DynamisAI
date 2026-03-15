package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.memory.EmbeddingVector;
import org.dynamisengine.ai.memory.SentenceEncoder;

/**
 * Minimal SentenceEncoder SPI example.
 *
 * A real encoder would tokenize text and run a transformer model to produce
 * semantic embeddings; this stub emits a constant 384-dim vector derived from
 * input length.
 */
public final class ExampleSentenceEncoder implements SentenceEncoder {

    private static final int DIM = 384;

    @Override
    public EmbeddingVector encode(String text) {
        float[] values = new float[DIM];
        float base = text == null ? 0f : clamp01(text.length() / 1000f);
        for (int i = 0; i < values.length; i++) {
            values[i] = base;
        }
        if (text != null && !text.isBlank()) {
            int bucket = Math.floorMod(text.hashCode(), DIM);
            values[bucket] = clamp01(base + 0.1f);
        }
        return new EmbeddingVector(values);
    }

    @Override
    public int dim() {
        return DIM;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
