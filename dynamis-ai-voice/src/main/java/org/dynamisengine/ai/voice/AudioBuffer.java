package org.dynamisengine.ai.voice;

import java.util.Objects;

/**
 * Immutable PCM buffer used by viseme extractors.
 */
public record AudioBuffer(float[] pcm, int sampleRate, int channels) {

    public AudioBuffer {
        Objects.requireNonNull(pcm, "pcm");
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be positive");
        }
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("channels must be 1 or 2");
        }
    }

    public float durationSeconds() {
        return (float) pcm.length / (sampleRate * channels);
    }
}
