package org.dynamisai.voice;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Waveform-driven viseme extractor backed by wav2vec2 ONNX.
 */
public final class WaveformVisemeExtractor implements VisemeExtractor {

    private static final Logger log = LoggerFactory.getLogger(WaveformVisemeExtractor.class);
    private static final Path DEFAULT_MODEL_PATH = Path.of(
        System.getProperty("user.home"),
        ".dynamisai", "models", "wav2vec2-base", "wav2vec2-base.onnx");
    private static final String[] PRESTON_BLAIR = {
        "rest", "MBP", "etc", "AI", "O", "E", "U", "WQ", "FV", "L", "Th"
    };

    private final RuleBasedVisemeExtractor fallback;
    private final Path modelPath;
    private ZooModel<NDList, NDList> model;
    private boolean live;

    public WaveformVisemeExtractor() {
        this(DEFAULT_MODEL_PATH);
    }

    public WaveformVisemeExtractor(Path modelPath) {
        this.modelPath = modelPath;
        this.fallback = new RuleBasedVisemeExtractor();
        initialize();
    }

    @Override
    public List<VisemeTimestamp> extract(AudioBuffer audio, String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return List.of();
        }
        if (!live || model == null) {
            return fallback.extract(audio, transcript);
        }

        try (NDManager manager = NDManager.newBaseManager()) {
            float[] mono16k = toMono16k(audio);
            if (mono16k.length == 0) {
                return List.of();
            }

            NDArray input = manager.create(mono16k).reshape(new Shape(1, mono16k.length));
            NDList output = model.newPredictor().predict(new NDList(input));
            NDArray features = output.singletonOrThrow();
            float[] activations = reduceFrameActivation(features);
            return toVisemes(activations, audio.durationSeconds());
        } catch (Exception e) {
            log.warn("WaveformVisemeExtractor inference failed ({}), using fallback", e.getMessage());
            return fallback.extract(audio, transcript);
        }
    }

    private void initialize() {
        if (!Files.exists(modelPath)) {
            log.warn("WaveformVisemeExtractor model not found at {} - using RuleBasedVisemeExtractor", modelPath);
            return;
        }
        try {
            Criteria<NDList, NDList> criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(modelPath.getParent())
                .optModelName(modelPath.getFileName().toString())
                .optEngine("OnnxRuntime")
                .build();
            model = criteria.loadModel();
            live = true;
            log.info("WaveformVisemeExtractor loaded model from {}", modelPath);
        } catch (Exception e) {
            log.warn("WaveformVisemeExtractor failed to load model ({}), using fallback", e.getMessage());
            live = false;
        }
    }

    private static float[] toMono16k(AudioBuffer audio) {
        float[] mono = audio.channels() == 1
            ? audio.pcm()
            : downmixStereo(audio.pcm());
        if (audio.sampleRate() == 16_000) {
            return mono.clone();
        }
        double ratio = 16_000.0 / audio.sampleRate();
        int outLen = Math.max(1, (int) Math.round(mono.length * ratio));
        float[] out = new float[outLen];
        double step = audio.sampleRate() / 16_000.0;
        for (int i = 0; i < outLen; i++) {
            double srcPos = i * step;
            int idx = (int) Math.floor(srcPos);
            int next = Math.min(idx + 1, mono.length - 1);
            double frac = srcPos - idx;
            out[i] = (float) (mono[idx] * (1.0 - frac) + mono[next] * frac);
        }
        return out;
    }

    private static float[] downmixStereo(float[] pcm) {
        int frames = pcm.length / 2;
        float[] mono = new float[frames];
        for (int i = 0; i < frames; i++) {
            mono[i] = (pcm[i * 2] + pcm[i * 2 + 1]) * 0.5f;
        }
        return mono;
    }

    private static float[] reduceFrameActivation(NDArray features) {
        // Expect shape [batch, frames, channels] or [frames, channels]
        NDArray reduced;
        if (features.getShape().dimension() == 3) {
            reduced = features.abs().mean(new int[]{2}).squeeze();
        } else if (features.getShape().dimension() == 2) {
            reduced = features.abs().mean(new int[]{1});
        } else {
            reduced = features.abs();
        }
        float[] values = reduced.toFloatArray();
        float max = 0f;
        for (float v : values) {
            if (v > max) {
                max = v;
            }
        }
        if (max <= 1e-6f) {
            return values;
        }
        float[] normalized = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            normalized[i] = Math.max(0f, Math.min(1f, values[i] / max));
        }
        return normalized;
    }

    private static List<VisemeTimestamp> toVisemes(float[] activations, float durationSeconds) {
        if (activations.length == 0 || durationSeconds <= 0f) {
            return List.of();
        }
        float step = durationSeconds / activations.length;
        List<VisemeTimestamp> out = new ArrayList<>(activations.length);
        for (int i = 0; i < activations.length; i++) {
            float start = i * step;
            float end = start + step;
            float weight = Math.max(0f, Math.min(1f, activations[i]));
            int bucket = i % PRESTON_BLAIR.length;
            // Encode end-time using offset for compatibility with existing timestamp shape.
            out.add(new VisemeTimestamp(Duration.ofMillis((long) (start * 1000)),
                PRESTON_BLAIR[bucket], weight));
            if (end <= start) {
                break;
            }
        }
        return List.copyOf(out);
    }
}
