package org.dynamisai.cognition;

/**
 * Runtime metrics from the last inference call — fed to BudgetGovernor telemetry.
 */
public record InferenceBackendMetrics(
    long lastCallLatencyMs,
    float tokensPerSecond,
    int totalCallCount,
    int failedCallCount,
    boolean available
) {
    public static InferenceBackendMetrics unavailable() {
        return new InferenceBackendMetrics(0, 0f, 0, 0, false);
    }
}
