package org.dynamisai.testkit;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.voice.PhysicalVoiceContext;
import org.dynamisai.voice.TTSPipeline;
import org.dynamisai.voice.VoiceRenderJob;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TTSPipelineContractTest {

    protected abstract TTSPipeline createSubject();

    @Test
    void renderReturnsNonNullFuture() {
        TTSPipeline tts = createSubject();
        DialogueResponse resp = new DialogueResponse(
            "Hello traveller.",
            AffectVector.neutral(),
            List.of(),
            List.of(),
            false
        );
        var future = tts.render(resp, PhysicalVoiceContext.calm(), EntityId.of(1L));
        assertNotNull(future);
    }

    @Test
    void renderCompletesWithinTimeout() throws Exception {
        TTSPipeline tts = createSubject();
        DialogueResponse resp = new DialogueResponse(
            "Halt!",
            AffectVector.neutral(),
            List.of(),
            List.of(),
            false
        );
        VoiceRenderJob job = tts.render(resp, PhysicalVoiceContext.calm(), EntityId.of(1L))
            .get(10, TimeUnit.SECONDS);
        assertNotNull(job);
    }

    @Test
    void renderJobContainsPrimaryAudio() throws Exception {
        TTSPipeline tts = createSubject();
        DialogueResponse resp = new DialogueResponse(
            "State your business.",
            AffectVector.neutral(),
            List.of(),
            List.of(),
            false
        );
        VoiceRenderJob job = tts.render(resp, PhysicalVoiceContext.calm(), EntityId.of(1L))
            .get(10, TimeUnit.SECONDS);
        assertNotNull(job.primarySpeech());
        assertTrue(job.primarySpeech().sampleRateHz() > 0);
    }
}
