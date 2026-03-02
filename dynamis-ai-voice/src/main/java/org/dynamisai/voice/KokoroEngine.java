package org.dynamisai.voice;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import org.dynamisai.cognition.AffectVector;
import org.dynamis.core.logging.DynamisLogger;

/**
 * Fast fallback TTS engine using Kokoro-82M (Apache 2.0).
 *
 * Kokoro is a lightweight single-pass model — lower quality than Chatterbox
 * but significantly faster. Used when Chatterbox is unavailable or over budget.
 */
public final class KokoroEngine {

    private static final DynamisLogger log = DynamisLogger.get(KokoroEngine.class);

    private final OnnxTtsSession session;
    private final TextTokenizer tokenizer;

    public KokoroEngine(TtsModelConfig config) {
        this.session = new OnnxTtsSession(config);
        this.tokenizer = new TextTokenizer();
    }

    public void initialize() { session.initialize(); }
    public boolean isAvailable() { return session.isInitialized(); }

    public float[] synthesize(String text) {
        return synthesize(text, null);
    }

    public float[] synthesize(String text, AffectVector affect) {
        if (!isAvailable())
            throw new TtsEngineException("KokoroEngine not initialized");

        NDManager manager = session.getManager();
        String styledText = AffectToVoiceStyle.applyToText(text, affect);
        int[] tokens = tokenizer.tokenizeFixed(styledText, session.config().maxTokens());

        try (Predictor<NDList, NDList> predictor = session.newPredictor()) {
            NDArray input = manager.create(tokens)
                .reshape(new Shape(1, tokens.length))
                .toType(DataType.INT32, false);
            NDList output = predictor.predict(new NDList(input));
            float[] pcm = output.get(0).squeeze().toFloatArray();
            log.debug(String.format("Kokoro synthesized %s samples", pcm.length));
            return pcm;
        } catch (Exception e) {
            throw new TtsEngineException("Kokoro synthesis failed: " + e.getMessage(), e);
        }
    }

    public int sampleRateHz() {
        return session.config().sampleRateHz();
    }

    public void close() { session.close(); }
}
