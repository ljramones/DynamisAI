package org.dynamisai.cognition;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zero-latency mock backend for unit tests and headless simulation baking.
 * Returns configurable canned JSON responses. Never contacts a real model.
 */
public final class MockInferenceBackend implements InferenceBackend {

    private static final String DEFAULT_RESPONSE =
        "{\"text\":\"I see.\",\"affect\":{\"valence\":0.0,\"arousal\":0.3," +
        "\"dominance\":0.5,\"sarcasm\":0.0,\"intensity\":0.3}," +
        "\"tags\":[],\"hints\":[]}";

    private final String cannedResponse;
    private final boolean shouldFail;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);
    private volatile long simulatedLatencyMs = 0;

    public MockInferenceBackend() {
        this(DEFAULT_RESPONSE, false);
    }

    public MockInferenceBackend(String cannedResponse, boolean shouldFail) {
        this.cannedResponse = cannedResponse;
        this.shouldFail = shouldFail;
    }

    /** Configure simulated latency for budget governor testing. */
    public void setSimulatedLatencyMs(long ms) {
        this.simulatedLatencyMs = ms;
    }

    @Override
    public String generate(String prompt, GenerationConfig config) throws InferenceException {
        callCount.incrementAndGet();
        if (simulatedLatencyMs > 0) {
            try { Thread.sleep(simulatedLatencyMs); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (shouldFail) {
            failCount.incrementAndGet();
            throw new InferenceException("MockInferenceBackend configured to fail");
        }
        return cannedResponse;
    }

    @Override public boolean isAvailable() { return !shouldFail; }
    @Override public boolean supportsStreaming() { return false; }
    @Override public String backendName() { return "MockInferenceBackend"; }

    @Override
    public InferenceBackendMetrics getMetrics() {
        return new InferenceBackendMetrics(
            simulatedLatencyMs, 0f,
            callCount.get(), failCount.get(),
            !shouldFail
        );
    }

    public int getCallCount() { return callCount.get(); }
}
