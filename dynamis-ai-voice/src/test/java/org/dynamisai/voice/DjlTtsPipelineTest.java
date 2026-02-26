package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests DjlTtsPipeline behaviour without live model files.
 * All engines will fail to initialize — tests verify graceful fallback behaviour.
 */
class DjlTtsPipelineTest {

    private static final EntityId SPEAKER = EntityId.of(1L);

    private DjlTtsPipeline buildPipeline() {
        return DjlTtsPipeline.create(
            TtsModelConfig.chatterbox("/tmp/no-chatterbox"),
            TtsModelConfig.bark("/tmp/no-bark"),
            TtsModelConfig.kokoro("/tmp/no-kokoro")
        );
    }

    private DialogueResponse response(String text) {
        return new DialogueResponse(text, AffectVector.neutral(),
            List.of("sigh"), List.of(), false);
    }

    @Test
    void pipelineCreatesWithoutModelFiles() {
        assertDoesNotThrow(this::buildPipeline);
    }

    @Test
    void unavailableWhenNoEnginesLoaded() {
        DjlTtsPipeline pipeline = buildPipeline();
        assertFalse(pipeline.isAvailable());
        pipeline.shutdown();
    }

    @Test
    void renderReturnsNonNullJobWhenNoEngines() throws Exception {
        DjlTtsPipeline pipeline = buildPipeline();
        try {
            var future = pipeline.render(response("Hello there."),
                PhysicalVoiceContext.calm(), SPEAKER);
            VoiceRenderJob job = future.get(3, TimeUnit.SECONDS);
            assertNotNull(job);
            assertEquals(SPEAKER, job.speaker());
        } finally {
            pipeline.shutdown();
        }
    }

    @Test
    void renderProducesVisemesFromExtractorEvenWithoutEngines() throws Exception {
        DjlTtsPipeline pipeline = buildPipeline();
        try {
            var future = pipeline.render(response("Hello there how are you."),
                PhysicalVoiceContext.calm(), SPEAKER);
            VoiceRenderJob job = future.get(3, TimeUnit.SECONDS);
            assertFalse(job.visemes().isEmpty(),
                "Visemes must be extracted even when no TTS engine is available");
        } finally {
            pipeline.shutdown();
        }
    }

    @Test
    void renderJobAffectMatchesResponse() throws Exception {
        DjlTtsPipeline pipeline = buildPipeline();
        try {
            DialogueResponse fearful = new DialogueResponse("Run!",
                AffectVector.fearful(), List.of(), List.of(), false);
            VoiceRenderJob job = pipeline.render(fearful, PhysicalVoiceContext.calm(),
                SPEAKER).get(3, TimeUnit.SECONDS);
            assertEquals(AffectVector.fearful(), job.affect());
        } finally {
            pipeline.shutdown();
        }
    }

    @Test
    void getFallbackBarkReturnsInstantly() {
        DjlTtsPipeline pipeline = buildPipeline();
        try {
            VoiceRenderJob bark = pipeline.getFallbackBark(SPEAKER, BarkType.ALERT);
            assertNotNull(bark);
            assertEquals(SPEAKER, bark.speaker());
        } finally {
            pipeline.shutdown();
        }
    }

    @Test
    void shutdownIsIdempotent() {
        DjlTtsPipeline pipeline = buildPipeline();
        assertDoesNotThrow(() -> {
            pipeline.shutdown();
            pipeline.shutdown();
        });
    }

    @Test
    void audioStreamFromPcmFloatsRoundTrips() {
        float[] original = {0.5f, -0.5f, 0.0f, 1.0f, -1.0f};
        AudioStream stream = AudioStream.fromPcmFloats("test", original, 22050);
        assertEquals(original.length * 2, stream.pcmData().length);
        float[] recovered = stream.toPcmFloats();
        assertEquals(original.length, recovered.length);
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], recovered[i], 0.001f,
                "PCM sample " + i + " round-trip mismatch");
        }
    }

    @Test
    void audioStreamDurationFromSampleCount() {
        float[] oneSec = new float[22050];
        AudioStream stream = AudioStream.fromPcmFloats("test", oneSec, 22050);
        assertEquals(1000L, stream.estimatedDuration().toMillis(), 10L);
    }

    @Test
    void ttsModelConfigValidatesPositiveSampleRate() {
        assertThrows(IllegalArgumentException.class, () ->
            new TtsModelConfig("bad", "/path", 0, 512));
    }

    @Test
    void textTokenizerProducesNonEmptyOutput() {
        TextTokenizer tokenizer = new TextTokenizer();
        int[] tokens = tokenizer.tokenize("Hello world");
        assertTrue(tokens.length > 0);
        assertEquals(1, tokens[tokens.length - 1]);
    }

    @Test
    void textTokenizerFixedLengthPadsCorrectly() {
        TextTokenizer tokenizer = new TextTokenizer();
        int[] fixed = tokenizer.tokenizeFixed("Hi", 10);
        assertEquals(10, fixed.length);
    }

    @Test
    void barkBuildNonverbalPrompt() {
        String prompt = BarkEngine.buildNonverbalPrompt(List.of("sigh", "laugh"));
        assertTrue(prompt.contains("[sigh]"));
        assertTrue(prompt.contains("[laugh]"));
    }

    @Test
    void barkBuildNonverbalPromptEmpty() {
        assertEquals("[neutral]", BarkEngine.buildNonverbalPrompt(List.of()));
        assertEquals("[neutral]", BarkEngine.buildNonverbalPrompt(null));
    }
}
