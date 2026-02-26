package org.dynamisai.voice;

import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;

import java.util.concurrent.CompletableFuture;

/**
 * Async TTS pipeline contract.
 * Primary: Chatterbox (MIT). Nonverbals: Bark (MIT). Fallback: Kokoro-82M (Apache 2.0).
 * Full DJL/ONNX wiring is a separate task — this interface and mock are stable contracts.
 */
public interface TTSPipeline {

    /**
     * Render speech from a DialogueResponse.
     * Returns immediately with a future — never blocks the simulation thread.
     * On failure, completes with a fallback bark.
     */
    CompletableFuture<VoiceRenderJob> render(DialogueResponse response,
                                              PhysicalVoiceContext physical,
                                              EntityId speaker);

    /**
     * Synchronous fallback bark — always instant, always available.
     * Used when TTS budget is exceeded (DegradeMode.FALLBACK in BudgetGovernor).
     */
    VoiceRenderJob getFallbackBark(EntityId speaker, BarkType type);

    /** Whether the full TTS backend is currently available. */
    boolean isAvailable();
}
