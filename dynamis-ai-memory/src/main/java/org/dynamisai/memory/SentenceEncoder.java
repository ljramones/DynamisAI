package org.dynamisai.memory;

/**
 * Converts text into a dense embedding vector.
 */
public interface SentenceEncoder {

    /**
     * Encode input text into an embedding vector.
     * Returns a zero vector for null/empty input.
     */
    EmbeddingVector encode(String text);

    /** Dimensionality of output vectors. */
    int dim();
}
