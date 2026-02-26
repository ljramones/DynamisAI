package org.dynamisai.demo;

import org.dynamisai.core.EntityId;
import org.dynamisai.memory.EmbeddingVector;
import org.dynamisai.memory.MemoryRecord;
import org.dynamisai.memory.SimilarityResult;
import org.dynamisai.memory.VectorMemoryStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo-local vector store adapter that avoids incubator runtime requirements.
 */
public final class DemoVectorMemoryStore implements VectorMemoryStore {

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public void store(MemoryRecord record, EmbeddingVector vector) {
        entries.put(record.id(), new Entry(record, vector.normalize()));
    }

    @Override
    public void remove(UUID id) {
        entries.remove(id);
    }

    @Override
    public CompletableFuture<List<SimilarityResult>> findSimilar(EmbeddingVector query, EntityId owner, int topK) {
        EmbeddingVector q = query.normalize();
        return CompletableFuture.supplyAsync(() -> entries.values().stream()
            .filter(e -> e.record.owner().equals(owner))
            .map(e -> new SimilarityResult(e.record, e.vector.cosineSimilarity(q)))
            .sorted(Comparator.naturalOrder())
            .limit(Math.max(0, topK))
            .toList());
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void close() {
        entries.clear();
    }

    @Override
    public int countForOwner(EntityId owner) {
        return (int) entries.values().stream()
            .filter(e -> e.record.owner().equals(owner))
            .count();
    }

    @Override
    public List<MemoryRecord> getAllForOwner(EntityId owner) {
        List<MemoryRecord> out = new ArrayList<>();
        for (Entry e : entries.values()) {
            if (e.record.owner().equals(owner)) {
                out.add(e.record);
            }
        }
        return out;
    }

    @Override
    public java.util.Optional<MemoryRecord> getById(UUID id) {
        Entry entry = entries.get(id);
        return entry == null ? java.util.Optional.empty() : java.util.Optional.of(entry.record);
    }

    private record Entry(MemoryRecord record, EmbeddingVector vector) {
    }
}
