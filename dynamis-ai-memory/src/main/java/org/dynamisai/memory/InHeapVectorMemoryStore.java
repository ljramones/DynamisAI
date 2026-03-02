package org.dynamisai.memory;

import org.dynamis.core.entity.EntityId;
import org.dynamis.core.logging.DynamisLogger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Legacy adapter — delegates to OffHeapVectorMemoryStore.
 *
 * Retained so DefaultMemoryLifecycleManager and legacy tests can construct it
 * with no call-site changes.
 */
public final class InHeapVectorMemoryStore implements VectorMemoryStore {

    private static final DynamisLogger log = DynamisLogger.get(InHeapVectorMemoryStore.class);

    private final SentenceEncoder encoder;
    private final OffHeapVectorMemoryStore delegate;

    public InHeapVectorMemoryStore() {
        this(new MockSentenceEncoder());
    }

    public InHeapVectorMemoryStore(SentenceEncoder encoder) {
        this.encoder = encoder;
        this.delegate = new OffHeapVectorMemoryStore(encoder.dim(), 64);
        log.info(String.format("InHeapVectorMemoryStore: encoder=%s dim=%s capacity=64", encoder.getClass().getSimpleName(), encoder.dim()));
    }

    /**
     * Store with auto-generated embedding from record summary.
     */
    @Override
    public void store(MemoryRecord record) {
        EmbeddingVector vec = encoder.encode(record.summary());
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
        EmbeddingVector q = encoder.encode(query);
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
    @Deprecated
    static EmbeddingVector keywordEmbedding(String text, int dim) {
        return MockSentenceEncoder.hashProject(text, dim);
    }
}
