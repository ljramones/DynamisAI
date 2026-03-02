package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BeliefModelRegistry {

    private final BeliefDecayPolicy defaultPolicy;
    private final ConcurrentMap<EntityId, BeliefModel> models = new ConcurrentHashMap<>();

    public BeliefModelRegistry(BeliefDecayPolicy defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public BeliefModel getOrCreate(EntityId entityId) {
        return models.computeIfAbsent(entityId, id -> new BeliefModel(id, defaultPolicy));
    }

    public Optional<BeliefModel> get(EntityId entityId) {
        return Optional.ofNullable(models.get(entityId));
    }

    public void remove(EntityId entityId) {
        models.remove(entityId);
    }

    public void decayAll(long currentTick) {
        models.values().forEach(model -> model.decay(currentTick));
    }

    public int registeredCount() {
        return models.size();
    }
}
