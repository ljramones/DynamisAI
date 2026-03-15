package org.dynamisengine.ai.voice;

import org.dynamisengine.core.exception.DynamisException;

/**
 * Thrown when a TTS engine fails to initialize or synthesize.
 * DjlTtsPipeline catches this and falls back to the next engine in the chain.
 */
public final class TtsEngineException extends DynamisException {
    public TtsEngineException(String message) { super(message); }
    public TtsEngineException(String message, Throwable cause) { super(message, cause); }
}
