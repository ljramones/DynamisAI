package org.dynamisai.extensions;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;
import org.dynamisai.voice.AudioStream;
import org.dynamisai.voice.BarkType;
import org.dynamisai.voice.PhysicalVoiceContext;
import org.dynamisai.voice.TTSPipeline;
import org.dynamisai.voice.VoiceRenderJob;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal TTSPipeline SPI example.
 *
 * A real pipeline would call a speech model (Kokoro, ElevenLabs, Azure, etc.)
 * and generate aligned visemes; this stub returns silent audio immediately.
 */
public final class ExampleTtsPipeline implements TTSPipeline {

    @Override
    public CompletableFuture<VoiceRenderJob> render(DialogueResponse response,
                                                     PhysicalVoiceContext physical,
                                                     EntityId speaker) {
        return CompletableFuture.completedFuture(jobFor(speaker, response.affect(), physical));
    }

    @Override
    public VoiceRenderJob getFallbackBark(EntityId speaker, BarkType type) {
        return jobFor(speaker, AffectVector.neutral(), PhysicalVoiceContext.calm());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static VoiceRenderJob jobFor(EntityId speaker,
                                         AffectVector affect,
                                         PhysicalVoiceContext physical) {
        byte[] silentPcm = new byte[22050 * 2];
        AudioStream primary = new AudioStream(
            "example-primary",
            silentPcm,
            22050,
            1,
            Duration.ofSeconds(1));
        AudioStream nonverbal = new AudioStream(
            "example-nonverbal",
            new byte[0],
            22050,
            1,
            Duration.ZERO);
        return new VoiceRenderJob(
            speaker,
            primary,
            nonverbal,
            List.of(),
            affect,
            physical,
            Duration.ofSeconds(1));
    }
}
