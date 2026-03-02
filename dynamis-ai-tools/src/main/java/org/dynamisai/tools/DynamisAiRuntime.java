package org.dynamisai.tools;

import org.dynamis.core.entity.EntityId;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamisai.cognition.BeliefModelRegistry;
import org.dynamisai.cognition.CognitionService;
import org.dynamisai.core.LodTier;
import org.dynamisai.lod.AILODPolicy;
import org.dynamisai.planning.IntentTypeRegistry;
import org.dynamisai.social.RumorIngestionHandler;
import org.dynamisai.social.SocialSystem;
import org.dynamisscripting.api.CanonLog;
import org.dynamisscripting.api.Chronicler;
import org.dynamisscripting.api.IntentBus;
import org.dynamisscripting.api.PerceptBus;
import org.dynamisscripting.api.value.Intent;
import org.dynamisscripting.api.value.Percept;
import org.dynamisscripting.api.value.WorldEvent;

/**
 * Assembly class that wires DynamisAI subsystems to the DynamisScripting runtime.
 *
 * <p>Dependency boundary: imports scripting-api only - never scripting-runtime,
 * scripting-oracle, scripting-canon, or any implementation module.
 *
 * <p>Lifecycle: construct -> wire() -> use -> shutdown()
 */
public final class DynamisAiRuntime {

    private static final DynamisLogger log = DynamisLogger.get(DynamisAiRuntime.class);

    private final CognitionService cognitionService;
    private final SocialSystem socialSystem;
    private final AILODPolicy lodPolicy;
    private final PerceptBus perceptBus;
    private final IntentBus intentBus;
    private final CanonLog canonLog;
    private final Chronicler chronicler;

    private boolean wired = false;

    private DynamisAiRuntime(Builder builder) {
        this.cognitionService = builder.cognitionService;
        this.socialSystem = builder.socialSystem;
        this.lodPolicy = builder.lodPolicy;
        this.perceptBus = builder.perceptBus;
        this.intentBus = builder.intentBus;
        this.canonLog = builder.canonLog;
        this.chronicler = builder.chronicler;
    }

    /**
     * Performs all subsystem wiring. Call once after construction.
     * Safe to call with null subsystems - absent subsystems are skipped.
     */
    public void wire() {
        if (wired) {
            throw new IllegalStateException("DynamisAiRuntime.wire() called more than once");
        }
        wired = true;

        if (cognitionService != null && canonLog != null) {
            cognitionService.setCanonTimeSource(canonLog::latestCanonTime);
            log.info("CanonTime source wired from CanonLog into CognitionService");
        }

        if (socialSystem != null && cognitionService != null) {
            BeliefModelRegistry registry = cognitionService.beliefRegistry();
            RumorIngestionHandler handler = new RumorIngestionHandler(registry);
            socialSystem.rumorPropagator().setDeliveryCallback(handler);
            log.info("RumorIngestionHandler wired to RumorPropagator");
        }

        if (chronicler != null) {
            chronicler.registerWorldEventListener(this::onWorldEvent);
            log.info("WorldEvent listener registered with Chronicler");
        }

        log.info("DynamisAiRuntime wiring complete");
    }

    /**
     * Registers an agent to receive percepts from PerceptBus.
     * Percepts are ingested as PERCEPT-sourced beliefs.
     */
    public void registerAgent(EntityId agentId) {
        if (perceptBus == null || cognitionService == null) {
            return;
        }
        perceptBus.subscribe(agentId, percept -> onPercept(agentId, percept));
        log.debug(String.format("Agent %s registered for percept subscription", agentId));
    }

    /**
     * Unregisters an agent from PerceptBus.
     */
    public void unregisterAgent(EntityId agentId) {
        if (perceptBus == null) {
            return;
        }
        perceptBus.unsubscribe(agentId);
        log.debug(String.format("Agent %s unregistered from percept subscription", agentId));
    }

    /**
     * Emits a planned intent to IntentBus.
     * Filters forbidden intents at LodTier >= 2.
     */
    public void emitIntent(Intent intent) {
        if (intentBus == null) {
            return;
        }

        LodTier tier = lodPolicy != null
            ? lodPolicy.currentTier(intent.agentId())
            : LodTier.TIER_0;

        if (tier.ordinal() >= 2
            && IntentTypeRegistry.isForbiddenAtTier2(intent.intentType())) {
            log.debug(String.format(
                "Intent %s suppressed for agent %s at %s",
                intent.intentType(), intent.agentId(), tier));
            return;
        }

        intentBus.emit(intent);
        log.debug(String.format(
            "Intent emitted: type=%s agent=%s tier=%s",
            intent.intentType(), intent.agentId(), tier));
    }

    /**
     * Advances social simulation by one tick (rumor propagation).
     * Call from the main simulation tick loop.
     */
    public void tick(long currentTick) {
        if (socialSystem != null) {
            socialSystem.tick(currentTick);
        }
    }

    private void onPercept(EntityId agentId, Percept percept) {
        if (cognitionService == null) {
            return;
        }
        cognitionService.beliefsFor(agentId).updateFromPercept(percept);
    }

    private void onWorldEvent(WorldEvent event) {
        log.debug(String.format("WorldEvent received: %s", event));
        // Concrete per-agent handling deferred to Step 7 integration.
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CognitionService cognitionService;
        private SocialSystem socialSystem;
        private AILODPolicy lodPolicy;
        private PerceptBus perceptBus;
        private IntentBus intentBus;
        private CanonLog canonLog;
        private Chronicler chronicler;

        public Builder cognitionService(CognitionService service) {
            this.cognitionService = service;
            return this;
        }

        public Builder socialSystem(SocialSystem system) {
            this.socialSystem = system;
            return this;
        }

        public Builder lodPolicy(AILODPolicy policy) {
            this.lodPolicy = policy;
            return this;
        }

        public Builder perceptBus(PerceptBus bus) {
            this.perceptBus = bus;
            return this;
        }

        public Builder intentBus(IntentBus bus) {
            this.intentBus = bus;
            return this;
        }

        public Builder canonLog(CanonLog log) {
            this.canonLog = log;
            return this;
        }

        public Builder chronicler(Chronicler chronicler) {
            this.chronicler = chronicler;
            return this;
        }

        public DynamisAiRuntime build() {
            return new DynamisAiRuntime(this);
        }
    }
}
