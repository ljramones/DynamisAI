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
import java.util.concurrent.atomic.AtomicLong;

public final class DefaultCognitionService implements CognitionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultCognitionService.class);

    /** Hard deadline — LLM must respond within this window or fallback is served. */
    private static final int DEFAULT_DEADLINE_MS = 300;

    /** Max concurrent in-flight inference requests. */
    private static final int MAX_CONCURRENT = 16;

    private final InferenceBackend backend;
    private final ResponseParser parser;
    private final ResponseCache cache;
    private final Map<EntityId, String> fallbackLines;
    private final int inferenceDeadlineMs;
    private final ExecutorService executor;
    private final Semaphore concurrencyLimit;
    private final AtomicInteger queueDepth = new AtomicInteger(0);
    private final BeliefModelRegistry beliefRegistry;
    private final AtomicLong deliberativeTickCounter = new AtomicLong(0L);

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines) {
        this(backend, parser, cache, fallbackLines, DEFAULT_DEADLINE_MS);
    }

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines,
                                   int inferenceDeadlineMs) {
        this.backend = backend;
        this.parser = parser;
        this.cache = cache;
        this.fallbackLines = fallbackLines;
        this.inferenceDeadlineMs = inferenceDeadlineMs;
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("cognition-", 0).factory());
        this.concurrencyLimit = new Semaphore(MAX_CONCURRENT);
        this.beliefRegistry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
    }

    /** Existing constructor — default deadline. */
    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser) {
        this(backend, parser, DEFAULT_DEADLINE_MS);
    }

    /** Additive constructor — configurable inference deadline in milliseconds. */
    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   int inferenceDeadlineMs) {
        this(backend, parser,
            new ResponseCache(256),
            new ConcurrentHashMap<>(),
            inferenceDeadlineMs);
    }

    /** Convenience constructor with defaults. */
    public DefaultCognitionService(InferenceBackend backend) {
        this(backend, new ResponseParser(), DEFAULT_DEADLINE_MS);
    }

    @Override
    public CompletableFuture<DialogueResponse> requestDialogue(DialogueRequest request) {
        long seed = request.snapshot() != null
            ? request.snapshot().seedFor(request.speaker())
            : 42L;
        return runInference(request, seed, false);
    }

    @Override
    public CompletableFuture<DialogueResponse> inferDeterministic(DialogueRequest request, long seed) {
        return runInference(request, seed, true);
    }

    private CompletableFuture<DialogueResponse> runInference(DialogueRequest request,
                                                             long seed,
                                                             boolean deterministic) {
        long tick = deliberativeTickCounter.incrementAndGet();
        if (tick % 60L == 0L) {
            beliefRegistry.decayAll(tick);
        }
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
                    InferenceRequest inferenceRequest = deterministic
                        ? InferenceRequest.seeded(request, seed)
                        : InferenceRequest.unseeded(request);
                    GenerationConfig config = deterministic
                        ? GenerationConfig.deterministic(seed)
                        : GenerationConfig.creative(seed);

                    String json = backend.generate(inferenceRequest, config);
                    DialogueResponse response = parser.parse(json);
                    cache.put(request.speaker(), response);
                    return response;
                } catch (InferenceException e) {
                    log.warn("Inference failed for {} — serving fallback: {}",
                        request.speaker(), e.getMessage());
                    return getFallback(request.speaker());
                }
            }, executor)
            .orTimeout(inferenceDeadlineMs, TimeUnit.MILLISECONDS)
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
    public BeliefModel beliefsFor(EntityId entityId) {
        return beliefRegistry.getOrCreate(entityId);
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
}
