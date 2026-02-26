package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;

import java.util.concurrent.CompletableFuture;

public interface CognitionService {

    /**
     * Request dialogue generation. Returns immediately with a future.
     * Never blocks the simulation thread.
     * On timeout or failure, the future completes with the fallback response.
     */
    CompletableFuture<DialogueResponse> requestDialogue(DialogueRequest request);

    /**
     * Synchronous fallback — always returns instantly from cache or canned line.
     * Used by BudgetGovernor when CognitionService task is in CACHED degrade mode.
     */
    DialogueResponse getFallback(EntityId speaker);

    /** Pre-populate the cache for an NPC — called at scene load time. */
    void warmCache(EntityId speaker, DialogueResponse response);

    /** Current queue depth — fed to BudgetGovernor telemetry. */
    int getQueueDepth();

    /** Shut down the virtual thread executor cleanly. */
    void shutdown();
}
