package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.cognition.GenerationConfig;
import org.dynamisengine.ai.cognition.InferenceBackend;
import org.dynamisengine.ai.cognition.InferenceBackendMetrics;
import org.dynamisengine.ai.cognition.InferenceException;

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
    public String generate(String prompt, GenerationConfig config) {
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
