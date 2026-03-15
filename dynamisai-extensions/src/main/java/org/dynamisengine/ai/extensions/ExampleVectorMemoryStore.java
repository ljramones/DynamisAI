package org.dynamisengine.ai.extensions;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.memory.EmbeddingVector;
import org.dynamisengine.ai.memory.MemoryRecord;
import org.dynamisengine.ai.memory.SimilarityResult;
import org.dynamisengine.ai.memory.VectorMemoryStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal VectorMemoryStore SPI example.
 *
 * A real store would maintain an ANN index (HNSW/IVF/etc.) and optimized
 * filtering; this stub uses an in-memory list and linear scans.
 */
public final class ExampleVectorMemoryStore implements VectorMemoryStore {

    private record Entry(MemoryRecord record, EmbeddingVector vector) {}

    private final List<Entry> entries = new ArrayList<>();
    private final ExampleSentenceEncoder encoder = new ExampleSentenceEncoder();

    @Override
    public synchronized void store(MemoryRecord record, EmbeddingVector vector) {
        entries.add(new Entry(record, vector));
    }

    @Override
    public synchronized void store(MemoryRecord record) {
        store(record, encoder.encode(record.summary()));
    }

    @Override
    public synchronized void remove(UUID id) {
        entries.removeIf(e -> e.record().id().equals(id));
    }

    @Override
    public synchronized CompletableFuture<List<SimilarityResult>> findSimilar(
            EmbeddingVector query, EntityId owner, int topK) {
        List<SimilarityResult> results = entries.stream()
            .filter(e -> e.record().owner().equals(owner))
            .map(e -> new SimilarityResult(e.record(), query.cosineSimilarity(e.vector())))
            .sorted(Comparator.naturalOrder())
            .limit(Math.max(0, topK))
            .toList();
        return CompletableFuture.completedFuture(results);
    }

    @Override
    public synchronized List<MemoryRecord> findSimilar(EntityId owner, String query, int maxResults) {
        EmbeddingVector queryVector = encoder.encode(query);
        return findSimilar(queryVector, owner, maxResults).join().stream()
            .map(SimilarityResult::record)
            .toList();
    }

    @Override
    public synchronized Optional<MemoryRecord> getById(UUID id) {
        return entries.stream()
            .map(Entry::record)
            .filter(r -> r.id().equals(id))
            .findFirst();
    }

    @Override
    public synchronized List<MemoryRecord> getAllForOwner(EntityId owner) {
        return entries.stream()
            .map(Entry::record)
            .filter(r -> r.owner().equals(owner))
            .toList();
    }

    @Override
    public synchronized int countForOwner(EntityId owner) {
        return (int) entries.stream().map(Entry::record).filter(r -> r.owner().equals(owner)).count();
    }

    @Override
    public synchronized int size() {
        return entries.size();
    }

    @Override
    public synchronized void close() {
        entries.clear();
    }
}
