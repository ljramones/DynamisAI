package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class VoicePipelineTest {

    private MockTTSPipeline pipeline;
    private EntityId speaker;

    @BeforeEach
    void setUp() {
        pipeline = new MockTTSPipeline();
        speaker = EntityId.of(1L);
    }

    private DialogueResponse response(String text) {
        return new DialogueResponse(text, AffectVector.neutral(),
            List.of("sigh"), List.of(), false);
    }

    @Test
    void renderReturnsVoiceRenderJob() throws Exception {
        var job = pipeline.render(response("Hello there."),
            PhysicalVoiceContext.calm(), speaker).get(2, TimeUnit.SECONDS);
        assertNotNull(job);
        assertEquals(speaker, job.speaker());
    }

    @Test
    void renderJobContainsAffectFromResponse() throws Exception {
        DialogueResponse r = new DialogueResponse("Test.",
            AffectVector.fearful(), List.of(), List.of(), false);
        var job = pipeline.render(r, PhysicalVoiceContext.calm(), speaker)
            .get(2, TimeUnit.SECONDS);
        assertEquals(AffectVector.fearful(), job.affect());
    }

    @Test
    void renderJobContainsNonEmptyVisemes() throws Exception {
        var job = pipeline.render(response("Hello world."),
            PhysicalVoiceContext.calm(), speaker).get(2, TimeUnit.SECONDS);
        assertFalse(job.visemes().isEmpty());
    }

    @Test
    void visemeWeightsAreInRange() throws Exception {
        var job = pipeline.render(response("Testing viseme weights."),
            PhysicalVoiceContext.calm(), speaker).get(2, TimeUnit.SECONDS);
        for (VisemeTimestamp v : job.visemes()) {
            assertTrue(v.weight() >= 0f && v.weight() <= 1f,
                "Viseme weight out of range: " + v.weight());
        }
    }

    @Test
    void visemeOffsetsAreNonNegative() throws Exception {
        var job = pipeline.render(response("Check offsets."),
            PhysicalVoiceContext.calm(), speaker).get(2, TimeUnit.SECONDS);
        for (VisemeTimestamp v : job.visemes()) {
            assertFalse(v.offset().isNegative(),
                "Negative viseme offset: " + v.offset());
        }
    }

    @Test
    void fallbackBarkIsInstant() {
        VoiceRenderJob bark = pipeline.getFallbackBark(speaker, BarkType.ALERT);
        assertNotNull(bark);
        assertEquals(speaker, bark.speaker());
    }

    @Test
    void renderCountTracksActualRenders() throws Exception {
        pipeline.render(response("One."), PhysicalVoiceContext.calm(), speaker)
            .get(2, TimeUnit.SECONDS);
        pipeline.render(response("Two."), PhysicalVoiceContext.calm(), speaker)
            .get(2, TimeUnit.SECONDS);
        assertEquals(2, pipeline.getRenderCount());
    }

    @Test
    void failingPipelineReturnsFallbackBark() throws Exception {
        MockTTSPipeline failing = new MockTTSPipeline(true);
        var job = failing.render(response("Should fail."),
            PhysicalVoiceContext.calm(), speaker).get(2, TimeUnit.SECONDS);
        assertNotNull(job);
        assertTrue(job.primarySpeech().label().startsWith("fallback-bark"));
    }

    @Test
    void physicalContextCalm() {
        PhysicalVoiceContext ctx = PhysicalVoiceContext.calm();
        assertFalse(ctx.isRunning());
        assertFalse(ctx.injured());
        assertEquals(0f, ctx.exertionLevel());
    }

    @Test
    void physicalContextSprinting() {
        PhysicalVoiceContext ctx = PhysicalVoiceContext.sprinting();
        assertTrue(ctx.isRunning());
        assertTrue(ctx.exertionLevel() > 0.5f);
    }

    @Test
    void physicalContextRejectsNegativeDistance() {
        assertThrows(IllegalArgumentException.class, () ->
            new PhysicalVoiceContext(false, false, false, false, -1f, 0f));
    }

    @Test
    void physicalContextRejectsExertionOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            new PhysicalVoiceContext(false, false, false, false, 3f, 1.5f));
    }

    @Test
    void visemeTimestampRejectsNegativeOffset() {
        assertThrows(IllegalArgumentException.class, () ->
            new VisemeTimestamp(Duration.ofMillis(-1), "AH", 0.5f));
    }

    @Test
    void visemeTimestampRejectsWeightOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            new VisemeTimestamp(Duration.ZERO, "AH", 1.5f));
    }

    @Test
    void voiceRenderJobStubIsNonNull() {
        VoiceRenderJob stub = VoiceRenderJob.stub(speaker, "Test text");
        assertNotNull(stub);
        assertNotNull(stub.primarySpeech());
        assertNotNull(stub.nonverbalTrack());
        assertNotNull(stub.visemes());
        assertNotNull(stub.affect());
    }

    @Test
    void audioStreamEmptyHasCorrectLabel() {
        AudioStream stream = AudioStream.empty("test-label");
        assertEquals("test-label", stream.label());
        assertEquals(0, stream.pcmData().length);
    }
}
