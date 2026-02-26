package org.dynamisai.cognition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InferenceBackendTest {

    @Test
    void mockBackendReturnsResponse() throws InferenceException {
        MockInferenceBackend backend = new MockInferenceBackend();
        String result = backend.generate("hello", GenerationConfig.creative(42L));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void mockBackendIsAvailable() {
        assertTrue(new MockInferenceBackend().isAvailable());
    }

    @Test
    void mockBackendTracksCallCount() throws InferenceException {
        MockInferenceBackend backend = new MockInferenceBackend();
        backend.generate("a", GenerationConfig.deterministic(1L));
        backend.generate("b", GenerationConfig.deterministic(2L));
        assertEquals(2, backend.getCallCount());
    }

    @Test
    void mockBackendFailingThrowsInferenceException() {
        MockInferenceBackend failing = new MockInferenceBackend("{}", true);
        assertThrows(InferenceException.class,
            () -> failing.generate("x", GenerationConfig.deterministic(1L)));
    }

    @Test
    void mockBackendUnavailableWhenConfiguredToFail() {
        assertFalse(new MockInferenceBackend("{}", true).isAvailable());
    }

    @Test
    void deterministicConfigHasZeroTemperature() {
        GenerationConfig cfg = GenerationConfig.deterministic(99L);
        assertEquals(0.0f, cfg.temperature());
        assertTrue(cfg.deterministicMode());
        assertEquals(99L, cfg.seed());
    }

    @Test
    void creativeConfigHasNonZeroTemperature() {
        GenerationConfig cfg = GenerationConfig.creative(1L);
        assertTrue(cfg.temperature() > 0f);
        assertFalse(cfg.deterministicMode());
    }

    @Test
    void generationConfigRejectsZeroTokens() {
        assertThrows(IllegalArgumentException.class,
            () -> new GenerationConfig(0, 0.5f, 1L, false));
    }

    @Test
    void generationConfigRejectsNegativeTemperature() {
        assertThrows(IllegalArgumentException.class,
            () -> new GenerationConfig(100, -0.1f, 1L, false));
    }

    @Test
    void generationConfigRejectsTemperatureAboveTwo() {
        assertThrows(IllegalArgumentException.class,
            () -> new GenerationConfig(100, 2.1f, 1L, false));
    }

    @Test
    void jlamaBackendNotAvailableBeforeInitialize() {
        JlamaInferenceBackend backend = new JlamaInferenceBackend("/tmp/model");
        assertFalse(backend.isAvailable());
    }

    @Test
    void jlamaBackendThrowsWhenNotInitialized() {
        JlamaInferenceBackend backend = new JlamaInferenceBackend("/tmp/model");
        assertThrows(InferenceException.class,
            () -> backend.generate("test", GenerationConfig.deterministic(1L)));
    }

    @Test
    void metricsUnavailableIsCorrect() {
        InferenceBackendMetrics m = InferenceBackendMetrics.unavailable();
        assertFalse(m.available());
        assertEquals(0, m.totalCallCount());
    }

    @Test
    void mockBackendNameIsSet() {
        assertEquals("MockInferenceBackend", new MockInferenceBackend().backendName());
    }

    // ── Jlama wired tests ─────────────────────────────────────────────────────

    @Test
    void jlamaBackendNotAvailableBeforeInitializeWired() {
        JlamaInferenceBackend backend = new JlamaInferenceBackend("/tmp/nonexistent-model");
        assertFalse(backend.isAvailable());
    }

    @Test
    void jlamaBackendThrowsWhenNotInitializedWired() {
        JlamaInferenceBackend backend = new JlamaInferenceBackend("/tmp/nonexistent-model");
        assertThrows(InferenceException.class,
            () -> backend.generate("test", GenerationConfig.deterministic(1L)));
    }

    @Test
    void jlamaBackendInitializeThrowsOnMissingModel() {
        JlamaInferenceBackend backend =
            new JlamaInferenceBackend("/tmp/nonexistent-model-path-xyz");
        assertThrows(InferenceException.class, backend::initialize);
    }

    @Test
    void jlamaBackendNameIncludesModelFilename() {
        JlamaInferenceBackend backend =
            new JlamaInferenceBackend("/models/llama3-8b-instruct.gguf");
        assertTrue(backend.backendName().contains("llama3-8b-instruct.gguf"));
    }

    @Test
    void jlamaBackendCloseIsIdempotent() {
        JlamaInferenceBackend backend =
            new JlamaInferenceBackend("/tmp/nonexistent-model");
        assertDoesNotThrow(() -> {
            backend.close();
            backend.close();
        });
    }

    @Test
    void jlamaMetricsBeforeInitialize() {
        JlamaInferenceBackend backend =
            new JlamaInferenceBackend("/tmp/nonexistent-model");
        InferenceBackendMetrics metrics = backend.getMetrics();
        assertFalse(metrics.available());
        assertEquals(0, metrics.totalCallCount());
    }
}
