package org.dynamisai.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioBufferTest {

    @Test
    void validConstruction() {
        AudioBuffer audio = new AudioBuffer(new float[32000], 16_000, 1);
        assertEquals(16_000, audio.sampleRate());
        assertEquals(1, audio.channels());
    }

    @Test
    void negativeSampleRateRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioBuffer(new float[1], -1, 1));
    }

    @Test
    void channelsAboveTwoRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new AudioBuffer(new float[2], 16_000, 3));
    }

    @Test
    void durationSecondsComputedCorrectly() {
        AudioBuffer mono = new AudioBuffer(new float[16_000], 16_000, 1);
        assertEquals(1.0f, mono.durationSeconds(), 0.0001f);

        AudioBuffer stereo = new AudioBuffer(new float[32_000], 16_000, 2);
        assertEquals(1.0f, stereo.durationSeconds(), 0.0001f);
    }
}
