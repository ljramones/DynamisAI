package org.dynamisai.extensions;

import org.dynamisai.cognition.GenerationConfig;
import org.dynamisai.cognition.InferenceBackend;
import org.dynamisai.testkit.InferenceBackendContractTest;
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
