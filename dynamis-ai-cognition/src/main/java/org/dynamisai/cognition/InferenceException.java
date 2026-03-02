package org.dynamisai.cognition;

import org.dynamis.core.exception.DynamisException;

/**
 * Thrown when an InferenceBackend cannot produce output.
 * CognitionService catches this and serves the fallback response.
 */
public final class InferenceException extends DynamisException {
    public InferenceException(String message) {
        super(message);
    }
    public InferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
