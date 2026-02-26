package org.dynamisai.extensions;

import org.dynamisai.cognition.GenerationConfig;
import org.dynamisai.cognition.InferenceBackend;
import org.dynamisai.cognition.InferenceBackendMetrics;
import org.dynamisai.cognition.InferenceException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal InferenceBackend SPI example.
 *
 * A real backend would call an LLM runtime or external API; this stub always
 * returns a deterministic placeholder response.
 */
public final class ExampleInferenceBackend implements InferenceBackend {

    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public String generate(String prompt, GenerationConfig config) throws InferenceException {
        calls.incrementAndGet();
        return "[ExampleInferenceBackend response]";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean supportsStreaming() {
        return false;
    }

    @Override
    public InferenceBackendMetrics getMetrics() {
        return new InferenceBackendMetrics(0L, 0f, calls.get(), 0, true);
    }

    @Override
    public String backendName() {
        return "example-inference-backend";
    }
}
