package org.dynamisai.memory;

import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Legacy adapter — delegates to OffHeapVectorMemoryStore.
 *
 * Retained so DefaultMemoryLifecycleManager can construct it without changes.
 * Uses a compact 32-dimensional embedding space.
 */
public final class InHeapVectorMemoryStore implements VectorMemoryStore {

    private static final Logger log = LoggerFactory.getLogger(InHeapVectorMemoryStore.class);

    /** Fixed dim for legacy keyword-hash embeddings. */
    private static final int LEGACY_DIM = 32;

    private final OffHeapVectorMemoryStore delegate;

    public InHeapVectorMemoryStore() {
        this.delegate = new OffHeapVectorMemoryStore(LEGACY_DIM, 64);
        log.info("InHeapVectorMemoryStore: delegating to OffHeapVectorMemoryStore " +
            "(dim={}, capacity=64)", LEGACY_DIM);
    }

    /**
     * Store with auto-generated embedding from record summary.
     */
    @Override
    public void store(MemoryRecord record) {
        EmbeddingVector vec = keywordEmbedding(record.summary(), LEGACY_DIM);
        delegate.store(record, vec);
    }

    @Override
    public void store(MemoryRecord record, EmbeddingVector vector) {
        delegate.store(record, vector);
    }

    @Override
    public void remove(UUID id) {
        delegate.remove(id);
    }

    @Override
    public CompletableFuture<List<SimilarityResult>> findSimilar(
        EmbeddingVector query, EntityId owner, int topK
    ) {
        return delegate.findSimilar(query, owner, topK);
    }

    @Override
    public List<MemoryRecord> findSimilar(EntityId owner, String query, int maxResults) {
        EmbeddingVector q = keywordEmbedding(query, LEGACY_DIM);
        try {
            return delegate.findSimilar(q, owner, maxResults)
                .get()
                .stream()
                .map(SimilarityResult::record)
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Optional<MemoryRecord> getById(UUID id) {
        return delegate.getById(id);
    }

    @Override
    public List<MemoryRecord> getAllForOwner(EntityId owner) {
        return delegate.getAllForOwner(owner);
    }

    @Override
    public int countForOwner(EntityId owner) {
        return delegate.countForOwner(owner);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * Deterministic character-hash projection.
     */
    static EmbeddingVector keywordEmbedding(String text, int dim) {
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
