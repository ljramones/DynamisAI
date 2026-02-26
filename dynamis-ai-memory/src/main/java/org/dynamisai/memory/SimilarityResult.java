package org.dynamisai.memory;

/**
 * A memory record paired with its cosine similarity score.
 * Returned by VectorMemoryStore.findSimilar().
 */
public record SimilarityResult(
    MemoryRecord record,
    float similarity
) implements Comparable<SimilarityResult> {

    @Override
    public int compareTo(SimilarityResult other) {
        // Natural order: descending similarity (most similar first)
        return Float.compare(other.similarity, this.similarity);
    }
}
