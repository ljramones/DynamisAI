package org.dynamisengine.ai.perception;

import org.dynamisengine.core.entity.EntityId;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe sensor profile registry keyed by entity id. */
public final class SensorProfileRegistry {

    private final ConcurrentMap<EntityId, SensorProfile> profiles =
        new ConcurrentHashMap<>();

    public void register(EntityId entityId, SensorProfile profile) {
        profiles.put(Objects.requireNonNull(entityId), Objects.requireNonNull(profile));
    }

    public void unregister(EntityId entityId) {
        profiles.remove(Objects.requireNonNull(entityId));
    }

    public SensorProfile getOrDefault(EntityId entityId) {
        return profiles.getOrDefault(Objects.requireNonNull(entityId), SensorProfile.defaultHuman());
    }

    public boolean isRegistered(EntityId entityId) {
        return profiles.containsKey(Objects.requireNonNull(entityId));
    }
}
