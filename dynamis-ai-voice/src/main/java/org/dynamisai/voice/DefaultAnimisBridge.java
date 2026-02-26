package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory AnimisBridge implementation.
 * Events are queued per agent and consumed by Animis each frame via pollEvents().
 * Thread-safe — DynamisAI AI threads produce; Animis render thread consumes.
 */
public final class DefaultAnimisBridge implements AnimisBridge {

    private static final Logger log = LoggerFactory.getLogger(DefaultAnimisBridge.class);

    private final Map<EntityId, ConcurrentLinkedQueue<AnimisBridgeEvent>> eventQueues =
        new ConcurrentHashMap<>();

    @Override
    public void submitVoiceJob(VoiceRenderJob job) {
        enqueue(job.speaker(), new AnimisBridgeEvent.VoiceJobEvent(job.speaker(), job));
        log.debug("VoiceJob submitted for agent {} — {} visemes, duration {}ms",
            job.speaker(), job.visemes().size(), job.estimatedDuration().toMillis());
    }

    @Override
    public void submitIntentSignal(EntityId agent, IntentSignal intent, Duration anticipateBy) {
        enqueue(agent, new AnimisBridgeEvent.IntentEvent(agent, intent, anticipateBy));
        log.debug("IntentSignal {} (confidence={}) for agent {} — anticipate {}ms",
            intent.type(), intent.confidence(), agent, anticipateBy.toMillis());
    }

    @Override
    public void pushAffectState(EntityId agent, AffectVector affect) {
        enqueue(agent, new AnimisBridgeEvent.AffectEvent(agent, affect));
    }

    @Override
    public List<AnimisBridgeEvent> pollEvents(EntityId agent) {
        var queue = eventQueues.get(agent);
        if (queue == null || queue.isEmpty()) return List.of();
        List<AnimisBridgeEvent> drained = new ArrayList<>();
        AnimisBridgeEvent event;
        while ((event = queue.poll()) != null) {
            drained.add(event);
        }
        return Collections.unmodifiableList(drained);
    }

    @Override
    public void clearEvents(EntityId agent) {
        var queue = eventQueues.remove(agent);
        if (queue != null) queue.clear();
    }

    private void enqueue(EntityId agent, AnimisBridgeEvent event) {
        eventQueues.computeIfAbsent(agent, k -> new ConcurrentLinkedQueue<>()).offer(event);
    }
}
