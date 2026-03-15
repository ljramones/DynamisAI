package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RumorPropagator {

    public static final int MAX_HOP_COUNT = 5;
    public static final float MIN_FIDELITY = 0.1f;
    public static final int PROPAGATION_INTERVAL_TICKS = 60;

    private final ReputationEngine reputationEngine;
    private final Map<EntityId, RumorQueue> queues = new ConcurrentHashMap<>();
    private volatile RumorDeliveryCallback deliveryCallback;

    public RumorPropagator(ReputationEngine reputationEngine) {
        this.reputationEngine = reputationEngine;
    }

    public Rumor seedRumor(ReputationEvent event) {
        EntityId origin = event.witness() != null ? event.witness() : event.target();
        return new Rumor(UUID.randomUUID(), event, origin, origin, 0, 1f, event.tick(), event.tick());
    }

    public void registerQueue(EntityId entityId, RumorQueue queue) {
        queues.put(entityId, queue);
    }

    public void unregisterQueue(EntityId entityId) {
        queues.remove(entityId);
    }

    public void setDeliveryCallback(RumorDeliveryCallback callback) {
        this.deliveryCallback = callback;
    }

    public void propagate(SocialGraph graph, long currentTick) {
        if (currentTick % PROPAGATION_INTERVAL_TICKS != 0) {
            return;
        }

        for (Map.Entry<EntityId, RumorQueue> entry : queues.entrySet()) {
            EntityId holder = entry.getKey();
            List<Rumor> rumors = entry.getValue().drain();
            for (Rumor rumor : rumors) {
                if (rumor.hopCount() >= MAX_HOP_COUNT || rumor.fidelity() < MIN_FIDELITY) {
                    continue;
                }

                ReputationEvent source = rumor.sourceEvent();
                float scaledMagnitude = Math.max(0f,
                    Math.min(1f, source.magnitude() * rumor.fidelity()));
                ReputationEvent heard = new ReputationEvent(
                    source.actor(),
                    holder,
                    source.type(),
                    scaledMagnitude,
                    currentTick,
                    false,
                    rumor.currentHolder()
                );
                reputationEngine.apply(heard, graph);

                for (EntityId neighbor : graph.knownEntities(holder)) {
                    if (neighbor.equals(holder)) {
                        continue;
                    }
                    if (graph.get(holder, neighbor).trust() <= 0f) {
                        continue;
                    }
                    RumorQueue q = queues.get(neighbor);
                    if (q == null) {
                        continue;
                    }
                    Rumor delivered = rumor.propagateTo(neighbor, currentTick);
                    q.enqueue(delivered);
                    RumorDeliveryCallback callback = deliveryCallback;
                    if (callback != null) {
                        float senderTrust = graph.get(holder, neighbor).trust();
                        callback.onDelivered(neighbor, delivered, senderTrust);
                    }
                }
            }
        }
    }

    public void post(EntityId recipient, Rumor rumor) {
        RumorQueue queue = queues.get(recipient);
        if (queue != null) {
            queue.enqueue(rumor);
        }
    }

    public int pendingRumorCount() {
        return queues.values().stream().mapToInt(RumorQueue::size).sum();
    }
}
