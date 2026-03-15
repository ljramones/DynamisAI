package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.cognition.Belief;
import org.dynamisengine.ai.cognition.CognitionService;
import org.dynamisengine.ai.cognition.DefaultCognitionService;
import org.dynamisengine.ai.cognition.DialogueResponse;
import org.dynamisengine.ai.cognition.MockInferenceBackend;
import org.dynamisengine.ai.core.BeliefSource;
import org.dynamisengine.ai.lod.AILODPolicy;
import org.dynamisengine.ai.lod.ImportanceScore;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamisAiRuntimeTest {

    @Test
    void wireCalledTwiceThrows() {
        DynamisAiRuntime runtime = DynamisAiRuntime.builder().build();
        runtime.wire();

        assertThrows(IllegalStateException.class, runtime::wire);
    }

    @Test
    void registerAgentWithNullPerceptBusDoesNotThrow() {
        DynamisAiRuntime runtime = DynamisAiRuntime.builder().build();
        assertDoesNotThrow(() -> runtime.registerAgent(EntityId.of(1L)));
    }

    @Test
    void emitIntentTier2ForbiddenDoesNotEmit() {
        CapturingIntentBus bus = new CapturingIntentBus();
        AILODPolicy tier2Plus = new AILODPolicy((id, snapshot) ->
            new ImportanceScore(id, 0.2f, org.dynamisengine.ai.core.LodTier.TIER_2, 1L));

        DynamisAiRuntime runtime = DynamisAiRuntime.builder()
            .intentBus(bus)
            .lodPolicy(tier2Plus)
            .build();

        runtime.emitIntent(intent(EntityId.of(1L), "combat.attack"));
        assertEquals(0, bus.emitted.size());
    }

    @Test
    void emitIntentTier0ForbiddenStillEmits() {
        CapturingIntentBus bus = new CapturingIntentBus();
        DynamisAiRuntime runtime = DynamisAiRuntime.builder()
            .intentBus(bus)
            .build();

        runtime.emitIntent(intent(EntityId.of(2L), "combat.attack"));
        assertEquals(1, bus.emitted.size());
    }

    @Test
    void emitIntentTier2NonForbiddenEmits() {
        CapturingIntentBus bus = new CapturingIntentBus();
        AILODPolicy tier2Plus = new AILODPolicy((id, snapshot) ->
            new ImportanceScore(id, 0.2f, org.dynamisengine.ai.core.LodTier.TIER_2, 1L));

        DynamisAiRuntime runtime = DynamisAiRuntime.builder()
            .intentBus(bus)
            .lodPolicy(tier2Plus)
            .build();

        runtime.emitIntent(intent(EntityId.of(3L), "locomotion.moveTo"));
        assertEquals(1, bus.emitted.size());
    }

    @Test
    void perceptCallbackCreatesPerceptSourcedBelief() {
        DefaultCognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
        CapturingPerceptBus bus = new CapturingPerceptBus();
        EntityId agent = EntityId.of(7L);
        try {
            DynamisAiRuntime runtime = DynamisAiRuntime.builder()
                .cognitionService(cognition)
                .perceptBus(bus)
                .build();

            runtime.registerAgent(agent);
            bus.deliver(Percept.of(agent, "SIGHT.ENTITY", "guard", 0.8, 99L));

            Optional<Belief> belief = cognition.beliefsFor(agent).getBelief("percept.sight_entity");
            assertTrue(belief.isPresent());
            assertEquals(BeliefSource.PERCEPT, belief.orElseThrow().source());
        } finally {
            cognition.shutdown();
        }
    }

    private static Intent intent(EntityId agentId, String intentType) {
        return Intent.of(
            agentId,
            intentType,
            List.of(),
            "test",
            1.0,
            CanonTime.of(1L, 0L),
            Intent.RequestedScope.PUBLIC
        );
    }

    private static final class CapturingIntentBus implements IntentBus {
        private final List<Intent> emitted = new CopyOnWriteArrayList<>();
        private final List<Consumer<Intent>> listeners = new CopyOnWriteArrayList<>();

        @Override
        public void emit(Intent intent) {
            emitted.add(intent);
            listeners.forEach(l -> l.accept(intent));
        }

        @Override
        public void subscribe(Consumer<Intent> listener) {
            listeners.add(listener);
        }
    }

    private static final class CapturingPerceptBus implements PerceptBus {
        private final List<Subscription> subscriptions = new ArrayList<>();

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

    @SuppressWarnings("unused")
    private static final class StubCanonLog implements CanonLog {
        @Override public void append(CanonEvent event) {}
        @Override public List<CanonEvent> query(CanonTime from, CanonTime to) { return List.of(); }
        @Override public List<CanonEvent> queryByCausalLink(String causalLink) { return List.of(); }
        @Override public Optional<CanonEvent> findByCommitId(long commitId) { return Optional.empty(); }
        @Override public CanonLog fork(long atCommitId) { return this; }
        @Override public void replay(long fromCommitId, Consumer<CanonEvent> handler) {}
        @Override public long latestCommitId() { return 1L; }
        @Override public CanonTime latestCanonTime() { return CanonTime.of(1L, 0L); }
    }

    @SuppressWarnings("unused")
    private static final class StubChronicler implements Chronicler {
        @Override public void tick(CanonTime currentTime) {}
        @Override public void registerWorldEventListener(Consumer<WorldEvent> listener) {}
    }
}
