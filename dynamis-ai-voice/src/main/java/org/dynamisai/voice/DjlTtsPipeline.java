package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamis.core.entity.EntityId;
import org.dynamis.core.logging.DynamisLogger;

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

    private static final DynamisLogger log = DynamisLogger.get(DjlTtsPipeline.class);

    private final ChatterboxEngine chatterbox;
    private final BarkEngine bark;
    private final KokoroEngine kokoro;
    private final VisemeExtractor visemeExtractor;
    private final BlendshapeMapper blendshapeMapper;
    private final ExecutorService executor;

    public DjlTtsPipeline(ChatterboxEngine chatterbox,
                          BarkEngine bark,
                          KokoroEngine kokoro) {
        this(chatterbox, bark, kokoro, new RuleBasedVisemeExtractor());
    }

    public DjlTtsPipeline(ChatterboxEngine chatterbox,
                          BarkEngine bark,
                          KokoroEngine kokoro,
                          VisemeExtractor visemeExtractor) {
        this.chatterbox = chatterbox;
        this.bark = bark;
        this.kokoro = kokoro;
        this.visemeExtractor = visemeExtractor;
        this.blendshapeMapper = new BlendshapeMapper(BlendshapeTable.defaultHumanoid());
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

            SynthesisResult primary = synthesizePrimary(text, response.affect());
            AudioStream nonverbal = synthesizeNonverbal(response.nonverbalTags());
            List<VisemeTimestamp> visemes = primary.visemes();
            Duration duration = primary.audio().estimatedDuration().isZero()
                ? Duration.ofMillis(500)
                : primary.audio().estimatedDuration();

            return new VoiceRenderJob(
                speaker, primary.audio(), nonverbal, visemes,
                response.affect(), physical, duration);
        }, executor);
    }

    @Override
    public VoiceRenderJob getFallbackBark(EntityId speaker, BarkType type) {
        String barkText = type.name().toLowerCase().replace("_", " ");
        SynthesisResult primary = synthesizePrimary(barkText, AffectVector.neutral());
        return new VoiceRenderJob(
            speaker, primary.audio(), AudioStream.empty("nonverbal-empty"),
            primary.visemes(),
            AffectVector.neutral(),
            PhysicalVoiceContext.calm(),
            primary.audio().estimatedDuration().isZero()
                ? Duration.ofMillis(500)
                : primary.audio().estimatedDuration());
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

    private SynthesisResult synthesizePrimary(String text, AffectVector affect) {
        if (chatterbox.isAvailable()) {
            try {
                float[] pcm = chatterbox.synthesize(text);
                AudioStream audio = AudioStream.fromPcmFloats("primary-speech", pcm,
                    chatterbox.sampleRateHz());
                List<VisemeTimestamp> visemes = visemeExtractor.extract(
                    new AudioBuffer(pcm, chatterbox.sampleRateHz(), 1), text);
                List<BlendshapeFrame> blendshapeFrames = blendshapeMapper.map(visemes, affect);
                return new SynthesisResult(audio, visemes, blendshapeFrames);
            } catch (TtsEngineException e) {
                log.warn(String.format("Chatterbox failed — falling back to Kokoro: %s", e.getMessage()));
            }
        }
        if (kokoro.isAvailable()) {
            try {
                float[] pcm = kokoro.synthesize(text, affect);
                AudioStream audio = AudioStream.fromPcmFloats("primary-speech-kokoro", pcm,
                    kokoro.sampleRateHz());
                List<VisemeTimestamp> visemes = visemeExtractor.extract(
                    new AudioBuffer(pcm, kokoro.sampleRateHz(), 1), text);
                List<BlendshapeFrame> blendshapeFrames = blendshapeMapper.map(visemes, affect);
                return new SynthesisResult(audio, visemes, blendshapeFrames);
            } catch (TtsEngineException e) {
                log.warn(String.format("Kokoro failed — returning empty stream: %s", e.getMessage()));
            }
        }
        AudioStream audio = AudioStream.empty("primary-speech-fallback");
        List<VisemeTimestamp> visemes = visemeExtractor.extract(
            new AudioBuffer(new float[0], audio.sampleRateHz(), audio.channelCount()), text);
        List<BlendshapeFrame> blendshapeFrames = blendshapeMapper.map(visemes, affect);
        return new SynthesisResult(audio, visemes, blendshapeFrames);
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
                log.debug(String.format("Bark nonverbal failed — skipping: %s", e.getMessage()));
            }
        }
        return AudioStream.empty("nonverbal-empty");
    }

    private static void tryInit(String name, TtsInitRunnable init) {
        try {
            init.run();
        } catch (TtsEngineException e) {
            log.info(String.format("%s engine not available — will use fallback: %s", name, e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface TtsInitRunnable {
        void run() throws TtsEngineException;
    }
}
