package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.cognition.GenerationConfig;
import org.dynamisengine.ai.cognition.InferenceBackend;
import org.dynamisengine.ai.testkit.InferenceBackendContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleInferenceBackendTest extends InferenceBackendContractTest {

    @Override
    protected InferenceBackend createSubject() {
        return new ExampleInferenceBackend();
    }

    @Test
    void methodsCanBeCalledDirectly() throws Exception {
        ExampleInferenceBackend backend = new ExampleInferenceBackend();
        assertTrue(backend.isAvailable());
        assertFalse(backend.supportsStreaming());
        assertNotNull(backend.backendName());
        assertNotNull(backend.getMetrics());
        assertNotNull(backend.generate("hi", GenerationConfig.minimal(1L)));
    }
}
