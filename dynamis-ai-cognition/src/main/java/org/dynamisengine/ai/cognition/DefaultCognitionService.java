package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.core.logging.DynamisLogger;
import org.dynamisengine.scripting.api.value.CanonTime;

import java.util.Map;
import java.util.Objects;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class DefaultCognitionService implements CognitionService {

    private static final DynamisLogger log = DynamisLogger.get(DefaultCognitionService.class);

    /** Hard deadline — LLM must respond within this window or fallback is served. */
    private static final int DEFAULT_DEADLINE_MS = 300;

    /** LLM results older than this many ticks are discarded as stale. */
    private static final long MAX_STALE_TICKS = 10L;

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
    private final AtomicLong completedCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final BeliefModelRegistry beliefRegistry;
    private final AtomicLong deliberativeTickCounter = new AtomicLong(0L);
    private final AtomicReference<Supplier<CanonTime>> canonTimeSource;

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines) {
        this(backend, parser, cache, fallbackLines, DEFAULT_DEADLINE_MS, () -> CanonTime.ZERO);
    }

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines,
                                   int inferenceDeadlineMs) {
        this(backend, parser, cache, fallbackLines, inferenceDeadlineMs, () -> CanonTime.ZERO);
    }

    public DefaultCognitionService(InferenceBackend backend,
                                   ResponseParser parser,
                                   ResponseCache cache,
                                   Map<EntityId, String> fallbackLines,
                                   int inferenceDeadlineMs,
                                   Supplier<CanonTime> canonTimeSource) {
        this.backend = backend;
        this.parser = parser;
        this.cache = cache;
        this.fallbackLines = fallbackLines;
        this.inferenceDeadlineMs = inferenceDeadlineMs;
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("cognition-", 0).factory());
        this.concurrencyLimit = new Semaphore(MAX_CONCURRENT);
        this.beliefRegistry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());
        this.canonTimeSource = new AtomicReference<>(
            Objects.requireNonNull(canonTimeSource, "canonTimeSource"));
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
            inferenceDeadlineMs,
            () -> CanonTime.ZERO);
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
            log.debug(String.format("Concurrency limit reached for %s — serving fallback", request.speaker()));
            return CompletableFuture.completedFuture(getFallback(request.speaker()));
        }
        final long requestTick = currentTick();

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
                    return parser.parse(json);
                } catch (InferenceException e) {
                    log.warn(String.format("Inference failed for %s — serving fallback: %s", request.speaker(), e.getMessage()));
                    return getFallback(request.speaker());
                }
            }, executor)
            .orTimeout(inferenceDeadlineMs, TimeUnit.MILLISECONDS)
            .handle((result, ex) -> {
                completedCount.incrementAndGet();
                if (ex != null) {
                    if (isTimeout(ex)) {
                        timeoutCount.incrementAndGet();
                        log.warn(String.format("Dialogue request timed out for %s — fallback", request.speaker()));
                    } else {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        log.warn(String.format("Dialogue request failed for %s — fallback: %s", request.speaker(), cause.getMessage()));
                    }
                    return getFallback(request.speaker());
                }

                long elapsedTicks = currentTick() - requestTick;
                if (elapsedTicks > MAX_STALE_TICKS) {
                    log.debug(String.format(
                        "Stale LLM result discarded for %s — %d ticks elapsed (max %d)",
                        request.speaker(), elapsedTicks, MAX_STALE_TICKS));
                    return getFallback(request.speaker());
                }

                cache.put(request.speaker(), result);
                return result;
            });

        return future.whenComplete((r, ex) -> {
            if (released.compareAndSet(false, true)) {
                concurrencyLimit.release();
                queueDepth.decrementAndGet();
            }
        });
    }

    private long currentTick() {
        CanonTime canonTime = canonTimeSource.get().get();
        return canonTime == null ? CanonTime.ZERO.tick() : canonTime.tick();
    }

    private static boolean isTimeout(Throwable ex) {
        if (ex instanceof TimeoutException) {
            return true;
        }
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof TimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void setCanonTimeSource(Supplier<CanonTime> source) {
        canonTimeSource.set(Objects.requireNonNull(source, "source"));
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
    public BeliefModelRegistry beliefRegistry() {
        return beliefRegistry;
    }

    /** Total completed inference requests (includes timeouts and successes). */
    public long getCompletedCount() { return completedCount.get(); }

    /** Total inference requests that timed out. */
    public long getTimeoutCount() { return timeoutCount.get(); }

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
