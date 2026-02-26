package org.dynamisai.cognition;

/**
 * Pluggable LLM inference backend.
 *
 * Implementations: JlamaBackend (production), OllamaBackend (dev), MockBackend (test/baking).
 * Swap via configuration — feature code never references a concrete implementation.
 */
public interface InferenceBackend {

    /**
     * Generate a response for the given prompt.
     * Must be thread-safe — called from virtual threads concurrently.
     *
     * @param prompt   The fully-assembled prompt string from ContextBuilder
     * @param config   Generation parameters including seed and determinism mode
     * @return         Raw response string — always non-null, never empty
     * @throws InferenceException if the backend fails and cannot produce output
     */
    String generate(String prompt, GenerationConfig config) throws InferenceException;

    /** Whether this backend can currently accept requests. */
    boolean isAvailable();

    /** Whether this backend supports streaming token output. */
    boolean supportsStreaming();

    /** Latest performance metrics — updated after each call. */
    InferenceBackendMetrics getMetrics();

    /** Human-readable name for logging and Inspector tooling. */
    String backendName();
}
