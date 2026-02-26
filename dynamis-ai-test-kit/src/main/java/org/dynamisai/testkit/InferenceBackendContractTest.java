package org.dynamisai.testkit;

import org.dynamisai.cognition.GenerationConfig;
import org.dynamisai.cognition.InferenceBackend;
import org.dynamisai.cognition.InferenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class InferenceBackendContractTest {

    protected abstract InferenceBackend createSubject();

    @Test
    void generateReturnsNonNullResult() throws Exception {
        InferenceBackend backend = createSubject();
        String text = backend.generate("Who are you?", GenerationConfig.minimal(42L));
        assertNotNull(text);
    }

    @Test
    void generateResultHasNonEmptyText() throws Exception {
        InferenceBackend backend = createSubject();
        String text = backend.generate("Halt! Who goes there?", GenerationConfig.minimal(7L));
        assertNotNull(text);
        assertFalse(text.isBlank());
    }

    @Test
    void multipleGenerateCallsDoNotThrow() {
        InferenceBackend backend = createSubject();
        assertDoesNotThrow(() -> {
            backend.generate("One", GenerationConfig.minimal(1L));
            backend.generate("Two", GenerationConfig.minimal(2L));
        });
    }

    @Test
    void backendReportsName() {
        InferenceBackend backend = createSubject();
        assertNotNull(backend.backendName());
        assertFalse(backend.backendName().isBlank());
    }

    @Test
    void backendExceptionTypeIsSupported() {
        assertNotNull(new InferenceException("x"));
    }
}
