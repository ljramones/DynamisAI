package org.dynamisai.voice;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * DJL ONNX session wrapper.
 * One session per TTS engine. Manages ZooModel + NDManager lifecycle.
 * Thread-safety: Predictor is NOT thread-safe — callers must synchronize or create per-call.
 */
public final class OnnxTtsSession implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(OnnxTtsSession.class);

    private final TtsModelConfig config;
    private NDManager manager;
    private ZooModel<NDList, NDList> model;
    private volatile boolean initialized = false;

    public OnnxTtsSession(TtsModelConfig config) {
        this.config = config;
    }

    /**
     * Load the ONNX model. Blocks until weights are mapped.
     * Throws TtsEngineException if the model path does not exist or is malformed.
     */
    public synchronized void initialize() throws TtsEngineException {
        if (initialized) return;
        Path modelPath = Paths.get(config.modelPath());
        log.info("Loading {} TTS model from: {}", config.label(), modelPath);
        try {
            manager = NDManager.newBaseManager();
            Criteria<NDList, NDList> criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(modelPath)
                .optEngine("OnnxRuntime")
                .build();
            model = criteria.loadModel();
            initialized = true;
            log.info("{} TTS model loaded", config.label());
        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            if (manager != null) {
                manager.close();
                manager = null;
            }
            throw new TtsEngineException(
                "Failed to load " + config.label() + " model: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new Predictor for one inference call.
     * Caller is responsible for closing the returned Predictor.
     */
    public Predictor<NDList, NDList> newPredictor() throws TtsEngineException {
        if (!initialized || model == null)
            throw new TtsEngineException(config.label() + " session not initialized");
        return model.newPredictor();
    }

    public NDManager getManager() throws TtsEngineException {
        if (!initialized || manager == null)
            throw new TtsEngineException(config.label() + " session not initialized");
        return manager;
    }

    public boolean isInitialized() { return initialized; }
    public TtsModelConfig config() { return config; }

    @Override
    public synchronized void close() {
        if (model != null) {
            try { model.close(); } catch (Exception ignored) {}
            model = null;
        }
        if (manager != null) {
            try { manager.close(); } catch (Exception ignored) {}
            manager = null;
        }
        initialized = false;
    }
}
