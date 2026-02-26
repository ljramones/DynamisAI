package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;

import java.time.Duration;
import java.util.List;

/**
 * The complete voice render package — everything AudioEngine and Animis need.
 * Produced by TTSPipeline, consumed by AudioEngine and AnimisBridge.
 */
public record VoiceRenderJob(
    EntityId speaker,
    AudioStream primarySpeech,
    AudioStream nonverbalTrack,
    List<VisemeTimestamp> visemes,
    AffectVector affect,
    PhysicalVoiceContext physicalContext,
    Duration estimatedDuration
) {
    /** Produce a stub job — all audio empty, neutral affect, calm physical state. */
    public static VoiceRenderJob stub(EntityId speaker, String text) {
        return new VoiceRenderJob(
            speaker,
            AudioStream.empty("primary-speech"),
            AudioStream.empty("nonverbal-track"),
            List.of(),
            AffectVector.neutral(),
            PhysicalVoiceContext.calm(),
            Duration.ofSeconds(2)
        );
    }
}
