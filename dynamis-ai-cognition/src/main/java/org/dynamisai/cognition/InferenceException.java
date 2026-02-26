package org.dynamisai.cognition;

/**
 * Thrown when an InferenceBackend cannot produce output.
 * CognitionService catches this and serves the fallback response.
 */
public final class InferenceException extends Exception {
    public InferenceException(String message) {
        super(message);
    }
    public InferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
