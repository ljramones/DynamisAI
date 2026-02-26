package org.dynamisai.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified facade for game-engine integration.
 */
public final class DynamisAiEngine {

    private static final Logger log = LoggerFactory.getLogger(DynamisAiEngine.class);

    private final DefaultWorldStateStore worldStore;
    private final BudgetGovernor governor;

    @SuppressWarnings("unused")
    private final Object perceptionSystem;
    @SuppressWarnings("unused")
    private final Object cognitionService;
    @SuppressWarnings("unused")
    private final Object memoryManager;
    @SuppressWarnings("unused")
    private final Object htnPlanner;
    @SuppressWarnings("unused")
    private final Object navigationSystem;
    @SuppressWarnings("unused")
    private final Object socialSystem;
    @SuppressWarnings("unused")
    private final Object crowdSystem;
    @SuppressWarnings("unused")
    private final Object ttsPipeline;
    @SuppressWarnings("unused")
    private final Object audioBridge;

    private final List<DialogueEvent> dialogueEvents = new ArrayList<>();
    private final List<AnimationSignal> animationSignals = new ArrayList<>();
    private final Map<EntityId, SteeringOutput> steeringOutputs = new HashMap<>();

    private boolean initialized;

    private DynamisAiEngine(Builder builder) {
        this.worldStore = builder.worldStore;
        this.governor = builder.governor;
        this.perceptionSystem = builder.perceptionSystem;
        this.cognitionService = builder.cognitionService;
        this.memoryManager = builder.memoryManager;
        this.htnPlanner = builder.htnPlanner;
        this.navigationSystem = builder.navigationSystem;
        this.socialSystem = builder.socialSystem;
        this.crowdSystem = builder.crowdSystem;
        this.ttsPipeline = builder.ttsPipeline;
        this.audioBridge = builder.audioBridge;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        log.info("DynamisAiEngine initialized");
    }

    public AIOutputFrame tick(GameEngineContext context) {
        long startNanos = System.nanoTime();

        dialogueEvents.clear();
        animationSignals.clear();
        steeringOutputs.clear();

        context.entityPositions().forEach((id, loc) ->
            worldStore.enqueueChange(new WorldChange.EntityStateChange(
                id, new EntityState(id, loc, Map.of()))));
        context.worldChanges().forEach(worldStore::enqueueChange);
        worldStore.commitTick();

        governor.runFrame(context.tick(), worldStore.getCurrentSnapshot());
        FrameBudgetReport report = governor.getLastFrameReport();
        if (report == null) {
            report = FrameBudgetReport.empty(context.tick());
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        return new AIOutputFrame(
            context.tick(),
            elapsedMs,
            Map.copyOf(steeringOutputs),
            List.copyOf(dialogueEvents),
            List.copyOf(animationSignals),
            worldStore.getCurrentSnapshot(),
            report
        );
    }

    public void shutdown() {
        log.info("DynamisAiEngine shutdown");
    }

    public void addDialogueEvent(DialogueEvent event) {
        dialogueEvents.add(event);
    }

    public void addAnimationSignal(AnimationSignal signal) {
        animationSignals.add(signal);
    }

    public void addSteeringOutput(EntityId id, SteeringOutput steering) {
        steeringOutputs.put(id, steering);
    }

    public WorldSnapshot currentSnapshot() {
        return worldStore.getCurrentSnapshot();
    }

    public long currentTick() {
        return worldStore.getCurrentTick();
    }

    public BudgetGovernor governor() {
        return governor;
    }

    public DefaultWorldStateStore worldStore() {
        return worldStore;
    }

    public static final class Builder {
        private DefaultWorldStateStore worldStore = new DefaultWorldStateStore();
        private BudgetGovernor governor = new DefaultBudgetGovernor(16);
        private Object perceptionSystem;
        private Object cognitionService;
        private Object memoryManager;
        private Object htnPlanner;
        private Object navigationSystem;
        private Object socialSystem;
        private Object crowdSystem;
        private Object ttsPipeline;
        private Object audioBridge;

        public Builder worldStore(DefaultWorldStateStore store) {
            this.worldStore = store;
            return this;
        }

        public Builder governor(BudgetGovernor governor) {
            this.governor = governor;
            return this;
        }

        public Builder perception(Object system) {
            this.perceptionSystem = system;
            return this;
        }

        public Builder cognition(Object system) {
            this.cognitionService = system;
            return this;
        }

        public Builder memory(Object system) {
            this.memoryManager = system;
            return this;
        }

        public Builder planning(Object system) {
            this.htnPlanner = system;
            return this;
        }

        public Builder navigation(Object system) {
            this.navigationSystem = system;
            return this;
        }

        public Builder social(Object system) {
            this.socialSystem = system;
            return this;
        }

        public Builder crowd(Object system) {
            this.crowdSystem = system;
            return this;
        }

        public Builder tts(Object system) {
            this.ttsPipeline = system;
            return this;
        }

        public Builder audio(Object system) {
            this.audioBridge = system;
            return this;
        }

        public DynamisAiEngine build() {
            return new DynamisAiEngine(this);
        }
    }
}
