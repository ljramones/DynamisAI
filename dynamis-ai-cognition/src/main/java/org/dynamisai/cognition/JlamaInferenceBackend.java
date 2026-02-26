package org.dynamisai.cognition;

import com.github.tjake.jlama.model.AbstractModel;
import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.model.functions.Generator;
import com.github.tjake.jlama.safetensors.DType;
import com.github.tjake.jlama.safetensors.prompt.PromptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Production LLM backend using Jlama — pure Java, in-process, offline-capable.
 *
 * Loads GGUF or safetensors weights via Jlama's Panama off-heap memory mapping.
 * Supports deterministic seeding and temperature control.
 *
 * Thread-safety: AbstractModel is NOT thread-safe. All generate() calls are
 * synchronized on this instance. For concurrent inference, create one
 * JlamaInferenceBackend per thread or use CognitionService's semaphore limit.
 */
public final class JlamaInferenceBackend implements InferenceBackend {

    private static final Logger log = LoggerFactory.getLogger(JlamaInferenceBackend.class);

    private final String modelPath;
    private final DType workingDType;
    private AbstractModel model;

    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);
    private volatile long lastLatencyMs = 0;
    private volatile float lastTokensPerSec = 0f;
    private volatile boolean initialized = false;

    /**
     * @param modelPath Path to model directory (safetensors) or GGUF file
     * @param workingDType Quantization type — F32 for quality, Q4 for speed
     */
    public JlamaInferenceBackend(String modelPath, DType workingDType) {
        this.modelPath = modelPath;
        this.workingDType = workingDType;
    }

    public JlamaInferenceBackend(String modelPath) {
        this(modelPath, DType.F32);
    }

    /**
     * Load model weights. Must be called before first generate().
     * Blocks until weights are mapped — typically 2-30s depending on model size.
     */
    public synchronized void initialize() throws InferenceException {
        if (initialized) {
            return;
        }
        try {
            log.info("Loading Jlama model from: {}", modelPath);
            long start = System.currentTimeMillis();
            model = ModelSupport.loadModel(
                new File(modelPath),
                workingDType,
                workingDType
            );
            long elapsed = System.currentTimeMillis() - start;
            log.info("Jlama model loaded in {}ms", elapsed);
            initialized = true;
        } catch (Exception e) {
            throw new InferenceException(
                "Failed to load Jlama model from: " + modelPath, e);
        }
    }

    @Override
    public synchronized String generate(String prompt,
                                        GenerationConfig config) throws InferenceException {
        if (!initialized || model == null) {
            throw new InferenceException(
                "JlamaInferenceBackend not initialized — call initialize() first");
        }

        callCount.incrementAndGet();
        long start = System.currentTimeMillis();
        StringBuilder streamed = new StringBuilder();

        try {
            UUID requestId = requestIdFor(prompt, config);
            Generator.Response response = model.generate(
                requestId,
                PromptContext.of(prompt),
                config.temperature(),
                config.maxTokens(),
                (token, time) -> {
                    if (token != null) {
                        streamed.append(token);
                    }
                    if (time != null && time > 0f) {
                        lastTokensPerSec = 1_000_000_000f / time;
                    }
                }
            );

            lastLatencyMs = System.currentTimeMillis() - start;

            String result = streamed.toString().trim();
            if (result.isEmpty() && response != null && response.responseText != null) {
                result = response.responseText.trim();
            }
            if (result.isEmpty()) {
                failCount.incrementAndGet();
                throw new InferenceException("Jlama returned empty response");
            }
            return result;

        } catch (InferenceException e) {
            throw e;
        } catch (Exception e) {
            failCount.incrementAndGet();
            lastLatencyMs = System.currentTimeMillis() - start;
            throw new InferenceException("Jlama inference failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public String backendName() {
        return "JlamaBackend[" + Path.of(modelPath).getFileName() + "]";
    }

    @Override
    public InferenceBackendMetrics getMetrics() {
        return new InferenceBackendMetrics(
            lastLatencyMs,
            lastTokensPerSec,
            callCount.get(),
            failCount.get(),
            initialized
        );
    }

    /** Release model resources. Call on engine shutdown. */
    public synchronized void close() {
        if (model != null) {
            try {
                model.close();
            } catch (Exception e) {
                log.warn("Exception closing Jlama model", e);
            }
            model = null;
            initialized = false;
        }
    }

    private UUID requestIdFor(String prompt, GenerationConfig config) {
        if (config.deterministicMode()) {
            String key = config.seed() + ":" + prompt;
            return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        }
        return new UUID(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
    }
}
