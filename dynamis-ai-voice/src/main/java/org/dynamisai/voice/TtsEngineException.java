package org.dynamisai.voice;

import org.dynamis.core.exception.DynamisException;

/**
 * Thrown when a TTS engine fails to initialize or synthesize.
 * DjlTtsPipeline catches this and falls back to the next engine in the chain.
 */
public final class TtsEngineException extends DynamisException {
    public TtsEngineException(String message) { super(message); }
    public TtsEngineException(String message, Throwable cause) { super(message, cause); }
}
