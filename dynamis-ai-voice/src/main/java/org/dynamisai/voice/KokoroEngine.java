package org.dynamisai.voice;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast fallback TTS engine using Kokoro-82M (Apache 2.0).
 *
 * Kokoro is a lightweight single-pass model — lower quality than Chatterbox
 * but significantly faster. Used when Chatterbox is unavailable or over budget.
 */
public final class KokoroEngine {

    private static final Logger log = LoggerFactory.getLogger(KokoroEngine.class);

    private final OnnxTtsSession session;
    private final TextTokenizer tokenizer;

    public KokoroEngine(TtsModelConfig config) {
        this.session = new OnnxTtsSession(config);
        this.tokenizer = new TextTokenizer();
    }

    public void initialize() throws TtsEngineException { session.initialize(); }
    public boolean isAvailable() { return session.isInitialized(); }

    public float[] synthesize(String text) throws TtsEngineException {
        if (!isAvailable())
            throw new TtsEngineException("KokoroEngine not initialized");

        NDManager manager = session.getManager();
        int[] tokens = tokenizer.tokenizeFixed(text, session.config().maxTokens());

        try (Predictor<NDList, NDList> predictor = session.newPredictor()) {
            NDArray input = manager.create(tokens)
                .reshape(new Shape(1, tokens.length))
                .toType(DataType.INT32, false);
            NDList output = predictor.predict(new NDList(input));
            float[] pcm = output.get(0).squeeze().toFloatArray();
            log.debug("Kokoro synthesized {} samples", pcm.length);
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
