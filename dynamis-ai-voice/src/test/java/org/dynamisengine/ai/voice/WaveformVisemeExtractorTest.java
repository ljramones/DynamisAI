package org.dynamisengine.ai.voice;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveformVisemeExtractorTest {

    @Test
    void constructWithMissingModelDoesNotThrow() {
        assertDoesNotThrow(() ->
            new WaveformVisemeExtractor(Path.of("/definitely/missing/wav2vec2-base.onnx")));
    }

    @Test
    void extractFallsBackToRuleBasedWhenModelMissing() {
        WaveformVisemeExtractor extractor =
            new WaveformVisemeExtractor(Path.of("/definitely/missing/wav2vec2-base.onnx"));
        AudioBuffer audio = new AudioBuffer(new float[1600], 16_000, 1);
        List<VisemeTimestamp> visemes = extractor.extract(audio, "hello guard");
        assertFalse(visemes.isEmpty());
    }

    @Test
    void fallbackOutputsValidTimestamps() {
        WaveformVisemeExtractor extractor =
            new WaveformVisemeExtractor(Path.of("/definitely/missing/wav2vec2-base.onnx"));
        AudioBuffer audio = new AudioBuffer(new float[1600], 16_000, 1);
        List<VisemeTimestamp> visemes = extractor.extract(audio, "hello guard");

        for (VisemeTimestamp v : visemes) {
            assertTrue(v.offset().toMillis() >= 0);
            assertTrue(v.weight() >= 0f && v.weight() <= 1f);
        }
    }

    @Test
    void emptyTranscriptReturnsEmpty() {
        WaveformVisemeExtractor extractor =
            new WaveformVisemeExtractor(Path.of("/definitely/missing/wav2vec2-base.onnx"));
        AudioBuffer audio = new AudioBuffer(new float[1600], 16_000, 1);
        assertTrue(extractor.extract(audio, "").isEmpty());
    }
}
