package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Production TTS pipeline using DJL ONNX Runtime.
 *
 * Fallback chain: Chatterbox -> Kokoro -> canned bark
 * Nonverbals:     Bark -> empty stream
 */
public final class DjlTtsPipeline implements TTSPipeline {

    private static final Logger log = LoggerFactory.getLogger(DjlTtsPipeline.class);

    private final ChatterboxEngine chatterbox;
    private final BarkEngine bark;
    private final KokoroEngine kokoro;
    private final VisemeExtractor visemeExtractor;
    private final ExecutorService executor;

    public DjlTtsPipeline(ChatterboxEngine chatterbox,
                          BarkEngine bark,
                          KokoroEngine kokoro) {
        this.chatterbox = chatterbox;
        this.bark = bark;
        this.kokoro = kokoro;
        this.visemeExtractor = new VisemeExtractor();
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("tts-", 0).factory());
    }

    /**
     * Factory — initialize all available engines.
     * Engines whose model paths do not exist are silently skipped.
     */
    public static DjlTtsPipeline create(TtsModelConfig chatterboxCfg,
                                        TtsModelConfig barkCfg,
                                        TtsModelConfig kokoroCfg) {
        ChatterboxEngine cb = new ChatterboxEngine(chatterboxCfg);
        BarkEngine bk = new BarkEngine(barkCfg);
        KokoroEngine ko = new KokoroEngine(kokoroCfg);

        tryInit("Chatterbox", cb::initialize);
        tryInit("Bark", bk::initialize);
        tryInit("Kokoro", ko::initialize);

        return new DjlTtsPipeline(cb, bk, ko);
    }

    @Override
    public CompletableFuture<VoiceRenderJob> render(DialogueResponse response,
                                                    PhysicalVoiceContext physical,
                                                    EntityId speaker) {
        return CompletableFuture.supplyAsync(() -> {
            String text = response.text();

            AudioStream primary = synthesizePrimary(text, response.affect());
            AudioStream nonverbal = synthesizeNonverbal(response.nonverbalTags());
            List<VisemeTimestamp> visemes = visemeExtractor.extract(text);
            Duration duration = visemeExtractor.estimateDuration(text);

            return new VoiceRenderJob(
                speaker, primary, nonverbal, visemes,
                response.affect(), physical, duration);
        }, executor);
    }

    @Override
    public VoiceRenderJob getFallbackBark(EntityId speaker, BarkType type) {
        String barkText = type.name().toLowerCase().replace("_", " ");
        AudioStream primary = synthesizePrimary(barkText, AffectVector.neutral());
        return new VoiceRenderJob(
            speaker, primary, AudioStream.empty("nonverbal-empty"),
            visemeExtractor.extract(barkText),
            AffectVector.neutral(),
            PhysicalVoiceContext.calm(),
            Duration.ofMillis(500));
    }

    @Override
    public boolean isAvailable() {
        return chatterbox.isAvailable() || kokoro.isAvailable();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        chatterbox.close();
        bark.close();
        kokoro.close();
    }

    private AudioStream synthesizePrimary(String text, AffectVector affect) {
        if (chatterbox.isAvailable()) {
            try {
                float[] pcm = chatterbox.synthesize(text);
                return AudioStream.fromPcmFloats("primary-speech", pcm,
                    chatterbox.sampleRateHz());
            } catch (TtsEngineException e) {
                log.warn("Chatterbox failed — falling back to Kokoro: {}", e.getMessage());
            }
        }
        if (kokoro.isAvailable()) {
            try {
                float[] pcm = kokoro.synthesize(text);
                return AudioStream.fromPcmFloats("primary-speech-kokoro", pcm,
                    kokoro.sampleRateHz());
            } catch (TtsEngineException e) {
                log.warn("Kokoro failed — returning empty stream: {}", e.getMessage());
            }
        }
        return AudioStream.empty("primary-speech-fallback");
    }

    private AudioStream synthesizeNonverbal(List<String> tags) {
        if (tags == null || tags.isEmpty())
            return AudioStream.empty("nonverbal-empty");
        if (bark.isAvailable()) {
            try {
                String prompt = BarkEngine.buildNonverbalPrompt(tags);
                float[] pcm = bark.synthesizeNonverbal(prompt);
                return AudioStream.fromPcmFloats("nonverbal-bark", pcm,
                    bark.sampleRateHz());
            } catch (TtsEngineException e) {
                log.debug("Bark nonverbal failed — skipping: {}", e.getMessage());
            }
        }
        return AudioStream.empty("nonverbal-empty");
    }

    private static void tryInit(String name, TtsInitRunnable init) {
        try {
            init.run();
        } catch (TtsEngineException e) {
            log.info("{} engine not available — will use fallback: {}", name, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface TtsInitRunnable {
        void run() throws TtsEngineException;
    }
}
