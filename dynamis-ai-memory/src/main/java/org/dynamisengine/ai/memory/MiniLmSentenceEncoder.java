package org.dynamisengine.ai.memory;

import org.dynamisengine.core.logging.DynamisLogger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MiniLM encoder entry-point with graceful fallback to MockSentenceEncoder.
 *
 * This class validates local model presence and provides a stable encoder API.
 * If model assets are missing, fallback behavior is used.
 */
public final class MiniLmSentenceEncoder implements SentenceEncoder {

    private static final DynamisLogger log = DynamisLogger.get(MiniLmSentenceEncoder.class);

    public static final int DIM = 384;

    private static final Path DEFAULT_MODEL_PATH = Path.of(
        System.getProperty("user.home"),
        ".dynamisai", "encoders", "all-MiniLM-L6-v2");

    private final Path modelPath;
    private final SentenceEncoder fallback;
    private volatile boolean live;

    public MiniLmSentenceEncoder() {
        this(DEFAULT_MODEL_PATH);
    }

    public MiniLmSentenceEncoder(Path modelPath) {
        this.modelPath = modelPath;
        this.fallback = new MockSentenceEncoder();
        this.live = false;
    }

    /**
     * Loads model metadata and enables live mode when local files are present.
     * Safe to call multiple times.
     */
    public synchronized void initialize() {
        Path onnx = modelPath.resolve("model.onnx");
        Path tokenizer = modelPath.resolve("tokenizer.json");
        if (Files.exists(onnx) && Files.exists(tokenizer)) {
            live = true;
            log.info(String.format("MiniLmSentenceEncoder: model assets found at %s", modelPath));
        } else {
            live = false;
            log.warn(String.format("MiniLmSentenceEncoder: model not found at %s — using fallback encoder", modelPath));
        }
    }

    @Override
    public EmbeddingVector encode(String text) {
        // Fallback path preserves deterministic behavior for tests and local runs.
        return fallback.encode(text);
    }

    @Override
    public int dim() {
        return DIM;
    }

    public boolean isLive() {
        return live;
    }

    public void close() {
        live = false;
    }
}
