package org.dynamisai.voice;

import java.util.List;

/**
 * Stable SPI for viseme extraction.
 */
public interface VisemeExtractor {

    /**
     * Extract viseme timing from rendered audio and transcript.
     *
     * @param audio rendered PCM buffer
     * @param transcript text that was synthesized
     * @return ascending viseme timestamps, never null
     */
    List<VisemeTimestamp> extract(AudioBuffer audio, String transcript);
}
