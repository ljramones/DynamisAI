package org.dynamisai.voice;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Nonverbal sound synthesis engine using Bark (MIT licence).
 *
 * Bark takes a text prompt with nonverbal tags (e.g., "[sigh]", "[laugh]")
 * and produces audio including those paralinguistic elements.
 */
public final class BarkEngine {

    private static final Logger log = LoggerFactory.getLogger(BarkEngine.class);

    private final OnnxTtsSession session;
    private final TextTokenizer tokenizer;

    public BarkEngine(TtsModelConfig config) {
        this.session = new OnnxTtsSession(config);
        this.tokenizer = new TextTokenizer();
    }

    public void initialize() throws TtsEngineException { session.initialize(); }
    public boolean isAvailable() { return session.isInitialized(); }

    /**
     * Synthesize nonverbal audio from a tagged prompt.
     */
    public float[] synthesizeNonverbal(String taggedPrompt) throws TtsEngineException {
        if (!isAvailable())
            throw new TtsEngineException("BarkEngine not initialized");

        NDManager manager = session.getManager();
        int[] tokens = tokenizer.tokenizeFixed(taggedPrompt, session.config().maxTokens());

        try (Predictor<NDList, NDList> predictor = session.newPredictor()) {
            NDArray input = manager.create(tokens)
                .reshape(new Shape(1, tokens.length))
                .toType(DataType.INT32, false);
            NDList output = predictor.predict(new NDList(input));
            float[] pcm = output.get(0).squeeze().toFloatArray();
            log.debug("Bark synthesized {} samples for nonverbal: {}", pcm.length, taggedPrompt);
            return pcm;
        } catch (Exception e) {
            throw new TtsEngineException("Bark synthesis failed: " + e.getMessage(), e);
        }
    }

    /** Build a nonverbal prompt from the tags in a DialogueResponse. */
    public static String buildNonverbalPrompt(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "[neutral]";
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append("[").append(tag).append("] ");
        }
        return sb.toString().trim();
    }

    public int sampleRateHz() {
        return session.config().sampleRateHz();
    }

    public void close() { session.close(); }
}
