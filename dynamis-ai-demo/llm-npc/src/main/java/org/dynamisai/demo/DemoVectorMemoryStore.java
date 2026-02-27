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
    /**
     * Stores a memory record and its associated embedding vector.
     * The vector is normalized before storage.
     *
     * @param record The memory record to store.
     * @param vector The vector embedding representing the record.
     */
    public void store(MemoryRecord record, EmbeddingVector vector) {
        entries.put(record.id(), new Entry(record, vector.normalize()));
    }

    @Override
    /**
     * Removes a memory record by its unique ID.
     *
     * @param id The UUID of the record to remove.
     */
    public void remove(UUID id) {
        entries.remove(id);
    }

    @Override
    /**
     * Finds the most similar memory records to a query vector for a specific owner.
     *
     * @param query The query embedding vector.
     * @param owner The entity ID of the memory owner.
     * @param topK  The maximum number of results to return.
     * @return A future completing with a list of similarity results, sorted by relevance.
     */
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
    /**
     * Returns the total number of records in the store.
     *
     * @return The record count.
     */
    public int size() {
        return entries.size();
    }

    @Override
    /**
     * Clears all entries from the store.
     */
    public void close() {
        entries.clear();
    }

    @Override
    /**
     * Counts the number of memory records owned by a specific entity.
     *
     * @param owner The entity ID of the owner.
     * @return The record count for that owner.
     */
    public int countForOwner(EntityId owner) {
        return (int) entries.values().stream()
            .filter(e -> e.record.owner().equals(owner))
            .count();
    }

    @Override
    /**
     * Retrieves all memory records owned by a specific entity.
     *
     * @param owner The entity ID of the owner.
     * @return A list of memory records for the owner.
     */
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
    /**
     * Retrieves a single memory record by its unique ID.
     *
     * @param id The UUID of the record.
     * @return An optional containing the record if found.
     */
    public java.util.Optional<MemoryRecord> getById(UUID id) {
        Entry entry = entries.get(id);
        return entry == null ? java.util.Optional.empty() : java.util.Optional.of(entry.record);
    }

    private record Entry(MemoryRecord record, EmbeddingVector vector) {
    }
}
