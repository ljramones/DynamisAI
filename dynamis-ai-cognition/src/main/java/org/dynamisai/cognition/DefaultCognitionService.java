package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultCognitionService implements CognitionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCognitionService.class);

    /** Hard deadline — LLM must respond within this window or fallback is served. */
    private static final long DEADLINE_MS = 300L;

    /** Max concurrent in-flight inference requests. */
    private static final int MAX_CONCURRENT = 16;

    private final InferenceBackend backend;
    private final ResponseParser parser;
    private final ResponseCache cache;
    private final Map<EntityId, String> fallbackLines;
    private final ExecutorService executor;
    private final Semaphore concurrencyLimit;
    private final AtomicInteger queueDepth = new AtomicInteger(0);

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines) {
        this.backend = backend;
        this.parser = parser;
        this.cache = cache;
        this.fallbackLines = fallbackLines;
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("cognition-", 0).factory());
        this.concurrencyLimit = new Semaphore(MAX_CONCURRENT);
    }

    /** Convenience constructor with defaults. */
    public DefaultCognitionService(InferenceBackend backend) {
        this(backend, new ResponseParser(),
             new ResponseCache(256),
             new ConcurrentHashMap<>());
    }

    @Override
    public CompletableFuture<DialogueResponse> requestDialogue(DialogueRequest request) {
        queueDepth.incrementAndGet();

        if (!concurrencyLimit.tryAcquire()) {
            queueDepth.decrementAndGet();
            log.debug("Concurrency limit reached for {} — serving fallback", request.speaker());
            return CompletableFuture.completedFuture(getFallback(request.speaker()));
        }

        AtomicBoolean released = new AtomicBoolean(false);
        CompletableFuture<DialogueResponse> future = CompletableFuture
            .supplyAsync(() -> {
                try {
                    String prompt = buildPrompt(request);
                    long seed = request.snapshot() != null
                        ? request.snapshot().seedFor(request.speaker())
                        : 42L;
                    GenerationConfig config = GenerationConfig.creative(seed);

                    String json = backend.generate(prompt, config);
                    DialogueResponse response = parser.parse(json);
                    cache.put(request.speaker(), response);
                    return response;
                } catch (InferenceException e) {
                    log.warn("Inference failed for {} — serving fallback: {}",
                        request.speaker(), e.getMessage());
                    return getFallback(request.speaker());
                }
            }, executor)
            .orTimeout(DEADLINE_MS, TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                if (!(ex instanceof TimeoutException)) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    log.warn("Dialogue request failed for {} — fallback: {}",
                        request.speaker(), cause.getMessage());
                } else {
                    log.warn("Dialogue request timed out for {} — fallback", request.speaker());
                }
                return getFallback(request.speaker());
            });

        return future.whenComplete((r, ex) -> {
            if (released.compareAndSet(false, true)) {
                concurrencyLimit.release();
                queueDepth.decrementAndGet();
            }
        });
    }

    @Override
    public DialogueResponse getFallback(EntityId speaker) {
        var cached = cache.get(speaker);
        if (cached.isPresent()) return cached.get();

        String line = fallbackLines.get(speaker);
        if (line != null) return DialogueResponse.fallback(line);

        return DialogueResponse.fallback("...");
    }

    @Override
    public void warmCache(EntityId speaker, DialogueResponse response) {
        cache.put(speaker, response);
    }

    @Override
    public int getQueueDepth() {
        return queueDepth.get();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String buildPrompt(DialogueRequest request) {
        return String.format(
            "You are an NPC. Player says: \"%s\". " +
            "Respond as JSON: {\"text\":\"...\",\"affect\":{\"valence\":0.0," +
            "\"arousal\":0.3,\"dominance\":0.5,\"sarcasm\":0.0,\"intensity\":0.3}," +
            "\"tags\":[],\"hints\":[]}",
            request.inputSpeech()
        );
    }
}
