package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded ring-buffer of DialogueEntry records for one ordered entity pair.
 *
 * Keyed by (EntityId min, EntityId max) to avoid duplicate (A,B)/(B,A) entries.
 * Capacity defaults to 20 entries per pair — older entries evicted when full.
 * Thread-safe via synchronized methods — history updates happen off the hot path.
 */
public final class DialogueHistory {

    private static final int DEFAULT_CAPACITY = 20;

    private final int capacity;

    // Key: canonical pair string "id_low:id_high"
    private final Map<String, ArrayDeque<DialogueEntry>> historyMap =
        new LinkedHashMap<>();

    public DialogueHistory() { this.capacity = DEFAULT_CAPACITY; }

    public DialogueHistory(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be >= 1");
        this.capacity = capacity;
    }

    /** Record a dialogue turn. Evicts oldest entry if capacity exceeded. */
    public synchronized void record(DialogueEntry entry) {
        String key = pairKey(entry.speaker(), entry.listener());
        ArrayDeque<DialogueEntry> deque =
            historyMap.computeIfAbsent(key, k -> new ArrayDeque<>(capacity));
        if (deque.size() >= capacity) deque.pollFirst();
        deque.addLast(entry);
    }

    /**
     * Returns the N most recent entries between two entities, newest first.
     * Returns empty list if no history exists.
     */
    public synchronized List<DialogueEntry> recent(EntityId a, EntityId b, int n) {
        String key = pairKey(a, b);
        ArrayDeque<DialogueEntry> deque = historyMap.get(key);
        if (deque == null || deque.isEmpty()) return List.of();

        List<DialogueEntry> result = new ArrayList<>(deque);
        Collections.reverse(result);
        return result.subList(0, Math.min(n, result.size()));
    }

    /** Total entries recorded across all pairs. */
    public synchronized int totalEntries() {
        return historyMap.values().stream().mapToInt(ArrayDeque::size).sum();
    }

    /** Number of distinct entity pairs with recorded history. */
    public synchronized int pairCount() { return historyMap.size(); }

    /** Clear all history for a pair — call on NPC despawn. */
    public synchronized void clearPair(EntityId a, EntityId b) {
        historyMap.remove(pairKey(a, b));
    }

    /**
     * Mean sentiment of recent N interactions between a pair.
     * Returns 0 if no history.
     */
    public synchronized float meanSentiment(EntityId a, EntityId b, int n) {
        List<DialogueEntry> entries = recent(a, b, n);
        if (entries.isEmpty()) return 0f;
        float sum = 0;
        for (DialogueEntry e : entries) sum += e.sentiment();
        return sum / entries.size();
    }

    private static String pairKey(EntityId a, EntityId b) {
        long lo = Math.min(a.value(), b.value());
        long hi = Math.max(a.value(), b.value());
        return lo + ":" + hi;
    }
}
