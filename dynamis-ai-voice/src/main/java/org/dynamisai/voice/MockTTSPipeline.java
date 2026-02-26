package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zero-latency mock TTS pipeline for unit tests and headless simulation baking.
 * Produces stub VoiceRenderJobs with configurable viseme timelines.
 */
public final class MockTTSPipeline implements TTSPipeline {

    private final boolean shouldFail;
    private final AtomicInteger renderCount = new AtomicInteger(0);
    private volatile long simulatedLatencyMs = 0;

    public MockTTSPipeline() { this(false); }

    public MockTTSPipeline(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    public void setSimulatedLatencyMs(long ms) { this.simulatedLatencyMs = ms; }
    public int getRenderCount() { return renderCount.get(); }

    @Override
    public CompletableFuture<VoiceRenderJob> render(DialogueResponse response,
                                                    PhysicalVoiceContext physical,
                                                    EntityId speaker) {
        if (shouldFail) {
            return CompletableFuture.completedFuture(getFallbackBark(speaker, BarkType.IDLE_COMMENT));
        }
        return CompletableFuture.supplyAsync(() -> {
            if (simulatedLatencyMs > 0) {
                try { Thread.sleep(simulatedLatencyMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            renderCount.incrementAndGet();
            return buildStubJob(speaker, response);
        });
    }

    @Override
    public VoiceRenderJob getFallbackBark(EntityId speaker, BarkType type) {
        return new VoiceRenderJob(
            speaker,
            AudioStream.empty("fallback-bark-" + type.name().toLowerCase()),
            AudioStream.empty("nonverbal-empty"),
            List.of(),
            AffectVector.neutral(),
            PhysicalVoiceContext.calm(),
            Duration.ofMillis(500)
        );
    }

    @Override
    public boolean isAvailable() { return !shouldFail; }

    private VoiceRenderJob buildStubJob(EntityId speaker, DialogueResponse response) {
        List<VisemeTimestamp> visemes = generateStubVisemes(response.text());
        Duration duration = Duration.ofMillis(response.text().length() * 60L);

        return new VoiceRenderJob(
            speaker,
            AudioStream.empty("primary-speech"),
            AudioStream.empty("nonverbal-track"),
            visemes,
            response.affect(),
            PhysicalVoiceContext.calm(),
            duration
        );
    }

    private List<VisemeTimestamp> generateStubVisemes(String text) {
        String[] visemePool = {"M", "EE", "AH", "F", "OH", "SH"};
        var result = new java.util.ArrayList<VisemeTimestamp>();
        int len = text == null ? 0 : text.length();
        for (int i = 0; i < len; i += 3) {
            result.add(new VisemeTimestamp(
                Duration.ofMillis(i * 60L),
                visemePool[(i / 3) % visemePool.length],
                0.8f
            ));
        }
        return List.copyOf(result);
    }
}
