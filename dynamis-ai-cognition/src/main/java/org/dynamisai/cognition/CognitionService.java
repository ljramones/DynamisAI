package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonTime;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface CognitionService {

    /**
     * Request dialogue generation. Returns immediately with a future.
     * Never blocks the simulation thread.
     * On timeout or failure, the future completes with the fallback response.
     */
    CompletableFuture<DialogueResponse> requestDialogue(DialogueRequest request);

    /**
     * Deterministic variant of dialogue inference with explicit seed.
     */
    CompletableFuture<DialogueResponse> inferDeterministic(DialogueRequest request, long seed);

    /**
     * Synchronous fallback — always returns instantly from cache or canned line.
     * Used by BudgetGovernor when CognitionService task is in CACHED degrade mode.
     */
    DialogueResponse getFallback(EntityId speaker);

    /** Pre-populate the cache for an NPC — called at scene load time. */
    void warmCache(EntityId speaker, DialogueResponse response);

    /** Current queue depth — fed to BudgetGovernor telemetry. */
    int getQueueDepth();

    /** Returns the belief model for an entity, creating one if absent. */
    BeliefModel beliefsFor(EntityId entityId);

    /** Returns the registry of agent belief models. */
    BeliefModelRegistry beliefRegistry();

    /** Sets the live CanonTime source. Called by runtime wiring after construction. */
    void setCanonTimeSource(Supplier<CanonTime> source);

    /** Shut down the virtual thread executor cleanly. */
    void shutdown();
}
