package org.dynamisai.memory;

import org.dynamisai.core.EntityId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Semantic vector store for NPC memory records.
 *
 * ASYNC-ONLY RULE: findSimilar() always returns CompletableFuture.
 * Implementations may use off-heap memory and SIMD — never block the
 * simulation thread on similarity queries.
 *
 * Implementations are thread-safe. store() and remove() may be called
 * from the simulation thread. findSimilar() runs on a virtual thread.
 */
public interface VectorMemoryStore {

    /**
     * Store a memory record with its embedding vector.
     * The vector is L2-normalized before storage.
     * Thread-safe.
     */
    void store(MemoryRecord record, EmbeddingVector vector);

    /**
     * Legacy convenience store path for lifecycle manager/tests.
     * Implementations may derive a deterministic embedding from summary.
     */
    default void store(MemoryRecord record) {
        throw new UnsupportedOperationException("store(record) not implemented");
    }

    /**
     * Remove a memory record. No-op if not present.
     * Thread-safe.
     */
    void remove(UUID id);

    /**
     * Async similarity search — returns top-K records most similar to query.
     * Results are sorted by descending similarity.
     * Never blocks the calling thread.
     */
    CompletableFuture<List<SimilarityResult>> findSimilar(
        EmbeddingVector query,
        EntityId owner,
        int topK
    );

    /** Legacy helper returning records only. */
    default List<MemoryRecord> findSimilar(EntityId owner, String query, int maxResults) {
        throw new UnsupportedOperationException("Legacy string findSimilar not implemented");
    }

    /** Legacy helper for record lookup. */
    default Optional<MemoryRecord> getById(UUID id) {
        throw new UnsupportedOperationException("getById not implemented");
    }

    /** Legacy helper for owner scan. */
    default List<MemoryRecord> getAllForOwner(EntityId owner) {
        throw new UnsupportedOperationException("getAllForOwner not implemented");
    }

    /** Legacy helper for owner count. */
    default int countForOwner(EntityId owner) {
        throw new UnsupportedOperationException("countForOwner not implemented");
    }

    /** Total records in the store across all owners. */
    int size();

    /** Free all off-heap resources. Safe to call multiple times. */
    void close();
}
