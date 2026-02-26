package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Bounded LRU response cache — keyed by EntityId.
 * Served when budget is exceeded (DegradeMode.CACHED).
 */
public final class ResponseCache {

    private final LinkedHashMap<EntityId, DialogueResponse> cache;

    public ResponseCache(int maxSize) {
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<EntityId, DialogueResponse> eldest) {
                return size() > maxSize;
            }
        };
    }

    public synchronized void put(EntityId id, DialogueResponse response) {
        cache.put(id, response);
    }

    public synchronized Optional<DialogueResponse> get(EntityId id) {
        return Optional.ofNullable(cache.get(id)).map(DialogueResponse::asCached);
    }

    public synchronized int size() { return cache.size(); }

    public synchronized void clear() { cache.clear(); }
}
