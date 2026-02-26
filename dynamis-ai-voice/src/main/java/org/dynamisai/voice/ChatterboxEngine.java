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
 * Primary speech synthesis engine using Chatterbox (MIT licence).
 *
 * STUB: Stage dispatch and shape inference are placeholders pending real model weight paths.
 * The DJL session loading and error handling are production-quality.
 */
public final class ChatterboxEngine {

    private static final Logger log = LoggerFactory.getLogger(ChatterboxEngine.class);

    private final OnnxTtsSession session;
    private final TextTokenizer tokenizer;

    public ChatterboxEngine(TtsModelConfig config) {
        this.session = new OnnxTtsSession(config);
        this.tokenizer = new TextTokenizer();
    }

    public void initialize() throws TtsEngineException {
        session.initialize();
    }

    public boolean isAvailable() { return session.isInitialized(); }

    /**
     * Synthesize speech from text.
     * Returns float[] PCM samples at config.sampleRateHz().
     */
    public float[] synthesize(String text) throws TtsEngineException {
        if (!isAvailable())
            throw new TtsEngineException("ChatterboxEngine not initialized");

        NDManager manager = session.getManager();
        int[] tokens = tokenizer.tokenizeFixed(text, session.config().maxTokens());

        try (Predictor<NDList, NDList> predictor = session.newPredictor()) {
            NDArray tokenTensor = manager.create(tokens)
                .reshape(new Shape(1, tokens.length))
                .toType(DataType.INT32, false);
            NDList input = new NDList(tokenTensor);
            NDList output = predictor.predict(input);

            NDArray audioBatch = output.get(0).squeeze();
            float[] pcm = audioBatch.toFloatArray();
            log.debug("Chatterbox synthesized {} samples for '{}...'",
                pcm.length, text.substring(0, Math.min(20, text.length())));
            return pcm;
        } catch (Exception e) {
            throw new TtsEngineException(
                "Chatterbox synthesis failed: " + e.getMessage(), e);
        }
    }

    public int sampleRateHz() {
        return session.config().sampleRateHz();
    }

    public void close() { session.close(); }
}
