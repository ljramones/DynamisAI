package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.cognition.Belief;
import org.dynamisengine.ai.cognition.DefaultCognitionService;
import org.dynamisengine.ai.cognition.MockInferenceBackend;
import org.dynamisengine.ai.core.BeliefSource;
import org.dynamisengine.ai.core.LodTier;
import org.dynamisengine.ai.lod.AILODPolicy;
import org.dynamisengine.ai.lod.ImportanceScore;
import org.dynamisengine.ai.social.DefaultSocialSystem;
import org.dynamisengine.ai.social.ReputationEvent;
import org.dynamisengine.ai.social.ReputationEventType;
import org.dynamisengine.ai.social.Rumor;
import org.dynamisengine.ai.social.RumorQueue;
import org.dynamisengine.scripting.api.CanonLog;
import org.dynamisengine.scripting.api.Chronicler;
import org.dynamisengine.scripting.api.IntentBus;
import org.dynamisengine.scripting.api.PerceptBus;
import org.dynamisengine.scripting.api.value.CanonEvent;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.dynamisengine.scripting.api.value.Intent;
import org.dynamisengine.scripting.api.value.Percept;
import org.dynamisengine.scripting.api.value.WorldEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end integration slice: verifies the full DynamisAI -> DynamisScripting pipeline.
 *
 * Pipeline under test:
 *   Percept delivered -> BeliefModel updated (PERCEPT source)
 *   Rumor delivered -> BeliefModel updated (RUMOR source)
 *   Intent emitted at TIER_0 -> IntentBus receives it
 *   Intent suppressed at TIER_2 (forbidden type) -> IntentBus does not receive it
 *   WorldEvent delivered -> Chronicler listener fires
 *   CanonTime source wired -> CognitionService uses CanonLog for staleness
 *   wire() called twice -> IllegalStateException
 */
class DynamisAiIntegrationTest {

    @Test
    void perceptPipelineCreatesPerceptBelief() {
        EntityId agent = EntityId.of(101L);
        DefaultCognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
        StubPerceptBus perceptBus = new StubPerceptBus();

        try {
            DynamisAiRuntime runtime = DynamisAiRuntime.builder()
                .cognitionService(cognition)
                .perceptBus(perceptBus)
                .build();

            runtime.registerAgent(agent);
            perceptBus.deliver(Percept.of(agent, "SIGHT.ENTITY", "guard", 0.9, 77L));

            Optional<Belief> belief = cognition.beliefsFor(agent).getBelief("percept.sight_entity");
            assertTrue(belief.isPresent());
            assertEquals(BeliefSource.PERCEPT, belief.orElseThrow().source());
            assertEquals(0.9f, belief.orElseThrow().confidence(), 0.0001f);
        } finally {
            cognition.shutdown();
        }
    }

    @Test
    void intentEmissionAtTier0Emits() {
        StubIntentBus intentBus = new StubIntentBus();
        Intent intent = intent(EntityId.of(102L), "dialogue.initiate");

        DynamisAiRuntime runtime = DynamisAiRuntime.builder()
            .intentBus(intentBus)
            .build();

        runtime.emitIntent(intent);

        assertEquals(1, intentBus.emitted().size());
        assertEquals(intent, intentBus.emitted().getFirst());
    }

    @Test
    void intentSuppressionAtTier2BlocksForbiddenIntent() {
        StubIntentBus intentBus = new StubIntentBus();
        AILODPolicy lodPolicy = new AILODPolicy((id, snapshot) ->
            new ImportanceScore(id, 0.2f, LodTier.TIER_2, 1L));

        DynamisAiRuntime runtime = DynamisAiRuntime.builder()
            .intentBus(intentBus)
            .lodPolicy(lodPolicy)
            .build();

        runtime.emitIntent(intent(EntityId.of(103L), "combat.attack"));

        assertEquals(0, intentBus.emitted().size());
    }

    @Test
    void rumorPipelineCreatesRumorBeliefOnRecipient() {
        EntityId actor = EntityId.of(201L);
        EntityId holder = EntityId.of(202L);
        EntityId recipient = EntityId.of(203L);

        DefaultCognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
        DefaultSocialSystem social = new DefaultSocialSystem();

        try {
            social.adjustTrust(holder, recipient, 0.7f);
            social.rumorPropagator().registerQueue(holder, new RumorQueue());
            social.rumorPropagator().registerQueue(recipient, new RumorQueue());

            DynamisAiRuntime runtime = DynamisAiRuntime.builder()
                .cognitionService(cognition)
                .socialSystem(social)
                .build();

            runtime.wire();

            ReputationEvent event = new ReputationEvent(
                actor,
                recipient,
                ReputationEventType.HELPED,
                1.0f,
                55L,
                true,
                holder
            );
            Rumor seeded = social.rumorPropagator().seedRumor(event);
            social.rumorPropagator().post(holder, seeded);

            runtime.tick(60L);

            String beliefKey = "rumor.helped.actor." + actor.id();
            Optional<Belief> belief = cognition.beliefsFor(recipient).getBelief(beliefKey);
            assertTrue(belief.isPresent());
            assertEquals(BeliefSource.RUMOR, belief.orElseThrow().source());
        } finally {
            cognition.shutdown();
        }
    }

    @Test
    void canonTimeWiringUsesCanonLogAndChroniclerListenerFires() throws ReflectiveOperationException {
        DefaultCognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
        StubCanonLog canonLog = new StubCanonLog(CanonTime.of(42L, 0L));
        StubChronicler chronicler = new StubChronicler();

        try {
            DynamisAiRuntime runtime = DynamisAiRuntime.builder()
                .cognitionService(cognition)
                .canonLog(canonLog)
                .chronicler(chronicler)
                .build();

            runtime.wire();

            Field field = DefaultCognitionService.class.getDeclaredField("canonTimeSource");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            AtomicReference<Supplier<CanonTime>> sourceRef =
                (AtomicReference<Supplier<CanonTime>>) field.get(cognition);
            assertNotNull(sourceRef);
            assertEquals(42L, sourceRef.get().get().tick());

            chronicler.emitWorldEvent(WorldEvent.of(
                "node.1",
                "story.beat",
                Map.of("phase", "intro"),
                1,
                CanonTime.of(42L, 0L)
            ));
            assertEquals(1, chronicler.deliveredCount());
        } finally {
            cognition.shutdown();
        }
    }

    @Test
    void wireCalledTwiceThrows() {
        DynamisAiRuntime runtime = DynamisAiRuntime.builder().build();
        runtime.wire();
        assertThrows(IllegalStateException.class, runtime::wire);
    }

    private static Intent intent(EntityId agentId, String intentType) {
        return Intent.of(
            agentId,
            intentType,
            List.of(),
            "integration-test",
            1.0,
            CanonTime.of(1L, 0L),
            Intent.RequestedScope.PUBLIC
        );
    }

    private static final class StubPerceptBus implements PerceptBus {
        private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

        @Override
        public void subscribe(EntityId agentId, Consumer<Percept> listener) {
            subscriptions.add(new Subscription(agentId, listener));
        }

        @Override
        public void unsubscribe(EntityId agentId) {
            subscriptions.removeIf(s -> s.agentId.equals(agentId));
        }

        @Override
        public void deliver(Percept percept) {
            subscriptions.stream()
                .filter(s -> s.agentId.equals(percept.agentId()))
                .forEach(s -> s.listener.accept(percept));
        }

        @Override
        public int subscriberCount() {
            return subscriptions.size();
        }

        private record Subscription(EntityId agentId, Consumer<Percept> listener) {}
    }

    private static final class StubIntentBus implements IntentBus {
        private final List<Intent> emitted = new CopyOnWriteArrayList<>();
        private final List<Consumer<Intent>> listeners = new CopyOnWriteArrayList<>();

        @Override
        public void emit(Intent intent) {
            emitted.add(intent);
            listeners.forEach(listener -> listener.accept(intent));
        }

        @Override
        public void subscribe(Consumer<Intent> listener) {
            listeners.add(listener);
        }

        List<Intent> emitted() {
            return emitted;
        }
    }

    private static final class StubCanonLog implements CanonLog {
        private final CanonTime time;

        private StubCanonLog(CanonTime time) {
            this.time = time;
        }

        @Override
        public void append(CanonEvent event) {
        }

        @Override
        public List<CanonEvent> query(CanonTime from, CanonTime to) {
            return List.of();
        }

        @Override
        public List<CanonEvent> queryByCausalLink(String causalLink) {
            return List.of();
        }

        @Override
        public Optional<CanonEvent> findByCommitId(long commitId) {
            return Optional.empty();
        }

        @Override
        public CanonLog fork(long atCommitId) {
            return this;
        }

        @Override
        public void replay(long fromCommitId, Consumer<CanonEvent> handler) {
        }

        @Override
        public long latestCommitId() {
            return 1L;
        }

        @Override
        public CanonTime latestCanonTime() {
            return time;
        }
    }

    private static final class StubChronicler implements Chronicler {
        private Consumer<WorldEvent> listener;
        private final AtomicInteger delivered = new AtomicInteger(0);

        @Override
        public void tick(CanonTime currentTime) {
        }

        @Override
        public void registerWorldEventListener(Consumer<WorldEvent> listener) {
            this.listener = listener;
        }

        void emitWorldEvent(WorldEvent event) {
            if (listener != null) {
                listener.accept(event);
                delivered.incrementAndGet();
            }
        }

        int deliveredCount() {
            return delivered.get();
        }
    }
}
