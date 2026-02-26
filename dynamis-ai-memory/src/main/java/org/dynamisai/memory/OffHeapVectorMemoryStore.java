package org.dynamisai.memory;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Off-heap VectorMemoryStore using Panama FFM + Vector API SIMD.
 */
public final class OffHeapVectorMemoryStore implements VectorMemoryStore {

    private static final Logger log = LoggerFactory.getLogger(OffHeapVectorMemoryStore.class);
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private final int dim;
    private final int floatsPerVec;

    private Arena arena;
    private MemorySegment segment;
    private int capacity;
    private final AtomicInteger count = new AtomicInteger(0);

    private final Map<UUID, Integer> idToSlot = new LinkedHashMap<>();
    private final Map<Integer, MemoryRecord> slotToRecord = new HashMap<>();
    private final ArrayDeque<Integer> freeSlots = new ArrayDeque<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ExecutorService executor =
        Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vec-mem-", 0).factory());

    private volatile boolean closed = false;

    public OffHeapVectorMemoryStore(int dim, int initialCapacity) {
        if (dim <= 0) {
            throw new IllegalArgumentException("dim must be > 0");
        }
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be > 0");
        }

        int lane = SPECIES.length();
        this.floatsPerVec = ((dim + lane - 1) / lane) * lane;
        this.dim = dim;
        this.capacity = initialCapacity;
        this.arena = Arena.ofShared();
        this.segment = allocateSegment(arena, (long) capacity * floatsPerVec);

        log.info("OffHeapVectorMemoryStore: dim={}, floatsPerVec={}, capacity={}, segment={}B, SIMD lanes={}",
            dim, floatsPerVec, capacity,
            (long) capacity * floatsPerVec * Float.BYTES, lane);
    }

    public OffHeapVectorMemoryStore(int dim) {
        this(dim, 256);
    }

    @Override
    public void store(MemoryRecord record, EmbeddingVector vector) {
        checkOpen();
        if (vector.dim() != dim) {
            throw new IllegalArgumentException(
                "Vector dim " + vector.dim() + " != store dim " + dim);
        }

        EmbeddingVector normalized = vector.normalize();
        float[] floats = normalized.toArray();

        rwLock.writeLock().lock();
        try {
            int slot = acquireSlot(record.id());
            writeVectorToSlot(slot, floats);
            slotToRecord.put(slot, record);
            count.set(idToSlot.size());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void store(MemoryRecord record) {
        store(record, InHeapVectorMemoryStore.keywordEmbedding(record.summary(), dim));
    }

    @Override
    public void remove(UUID id) {
        checkOpen();
        rwLock.writeLock().lock();
        try {
            Integer slot = idToSlot.remove(id);
            if (slot != null) {
                slotToRecord.remove(slot);
                freeSlots.push(slot);
                zeroSlot(slot);
                count.set(idToSlot.size());
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public CompletableFuture<List<SimilarityResult>> findSimilar(
        EmbeddingVector query,
        EntityId owner,
        int topK
    ) {
        checkOpen();
        if (query.dim() != dim) {
            throw new IllegalArgumentException(
                "Query dim " + query.dim() + " != store dim " + dim);
        }
        EmbeddingVector normalizedQuery = query.normalize();
        float[] qFloats = padToLaneMultiple(normalizedQuery.toArray());

        return CompletableFuture.supplyAsync(() -> {
            rwLock.readLock().lock();
            try {
                return scanSimilarity(qFloats, owner, topK);
            } finally {
                rwLock.readLock().unlock();
            }
        }, executor);
    }

    @Override
    public List<MemoryRecord> findSimilar(EntityId owner, String query, int maxResults) {
        EmbeddingVector q = InHeapVectorMemoryStore.keywordEmbedding(query, dim);
        try {
            return findSimilar(q, owner, maxResults).get(5, TimeUnit.SECONDS).stream()
                .map(SimilarityResult::record)
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Optional<MemoryRecord> getById(UUID id) {
        rwLock.readLock().lock();
        try {
            Integer slot = idToSlot.get(id);
            if (slot == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(slotToRecord.get(slot));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<MemoryRecord> getAllForOwner(EntityId owner) {
        rwLock.readLock().lock();
        try {
            return slotToRecord.values().stream()
                .filter(r -> r.owner().equals(owner))
                .toList();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int countForOwner(EntityId owner) {
        rwLock.readLock().lock();
        try {
            return (int) slotToRecord.values().stream()
                .filter(r -> r.owner().equals(owner))
                .count();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        return count.get();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        rwLock.writeLock().lock();
        try {
            arena.close();
            idToSlot.clear();
            slotToRecord.clear();
            freeSlots.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
        log.info("OffHeapVectorMemoryStore closed");
    }

    private List<SimilarityResult> scanSimilarity(float[] query, EntityId owner, int topK) {
        if (topK <= 0) {
            return List.of();
        }
        PriorityQueue<SimilarityResult> heap = new PriorityQueue<>(topK + 1);

        for (Map.Entry<Integer, MemoryRecord> entry : slotToRecord.entrySet()) {
            int slot = entry.getKey();
            MemoryRecord record = entry.getValue();

            if (!record.owner().equals(owner)) {
                continue;
            }

            float similarity = simdDotProduct(query, slot);
            if (heap.size() < topK) {
                heap.offer(new SimilarityResult(record, similarity));
            } else if (!heap.isEmpty() && similarity > heap.peek().similarity()) {
                heap.poll();
                heap.offer(new SimilarityResult(record, similarity));
            }
        }

        List<SimilarityResult> results = new ArrayList<>(heap);
        Collections.sort(results);
        return results;
    }

    private float simdDotProduct(float[] query, int slot) {
        long offsetBytes = (long) slot * floatsPerVec * Float.BYTES;
        int laneLen = SPECIES.length();
        float sum = 0f;
        int i = 0;

        for (; i <= floatsPerVec - laneLen; i += laneLen) {
            FloatVector va = FloatVector.fromMemorySegment(
                SPECIES,
                segment,
                offsetBytes + (long) i * Float.BYTES,
                ByteOrder.nativeOrder());
            FloatVector vb = FloatVector.fromArray(SPECIES, query, i);
            sum += va.mul(vb).reduceLanes(VectorOperators.ADD);
        }

        for (; i < dim; i++) {
            float stored = segment.get(ValueLayout.JAVA_FLOAT,
                offsetBytes + (long) i * Float.BYTES);
            sum += stored * query[i];
        }

        return sum;
    }

    private int acquireSlot(UUID id) {
        Integer existing = idToSlot.get(id);
        if (existing != null) {
            return existing;
        }

        if (freeSlots.isEmpty() && idToSlot.size() >= capacity) {
            ensureCapacity();
        }

        int slot = freeSlots.isEmpty() ? idToSlot.size() : freeSlots.pop();
        idToSlot.put(id, slot);
        return slot;
    }

    private void writeVectorToSlot(int slot, float[] floats) {
        long offsetBytes = (long) slot * floatsPerVec * Float.BYTES;
        for (int i = 0; i < floats.length; i++) {
            segment.set(ValueLayout.JAVA_FLOAT,
                offsetBytes + (long) i * Float.BYTES, floats[i]);
        }
        for (int i = floats.length; i < floatsPerVec; i++) {
            segment.set(ValueLayout.JAVA_FLOAT,
                offsetBytes + (long) i * Float.BYTES, 0f);
        }
    }

    private void zeroSlot(int slot) {
        long offsetBytes = (long) slot * floatsPerVec * Float.BYTES;
        for (int i = 0; i < floatsPerVec; i++) {
            segment.set(ValueLayout.JAVA_FLOAT,
                offsetBytes + (long) i * Float.BYTES, 0f);
        }
    }

    private void ensureCapacity() {
        int newCapacity = capacity * 2;
        log.info("OffHeapVectorMemoryStore: resizing {} -> {} vectors", capacity, newCapacity);

        MemorySegment newSegment = allocateSegment(arena, (long) newCapacity * floatsPerVec);
        long copyBytes = (long) capacity * floatsPerVec * Float.BYTES;
        newSegment.asSlice(0, copyBytes).copyFrom(segment.asSlice(0, copyBytes));
        segment = newSegment;
        capacity = newCapacity;
    }

    private static MemorySegment allocateSegment(Arena arena, long floatCount) {
        return arena.allocate(floatCount * Float.BYTES, Float.BYTES);
    }

    private float[] padToLaneMultiple(float[] src) {
        if (src.length == floatsPerVec) {
            return src;
        }
        float[] padded = new float[floatsPerVec];
        System.arraycopy(src, 0, padded, 0, src.length);
        return padded;
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("OffHeapVectorMemoryStore has been closed");
        }
    }
}
