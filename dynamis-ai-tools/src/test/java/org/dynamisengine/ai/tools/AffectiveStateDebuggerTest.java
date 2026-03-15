package org.dynamisengine.ai.tools;

import io.vavr.collection.HashMap;
import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.ai.cognition.BeliefModel;
import org.dynamisengine.ai.cognition.BeliefModelRegistry;
import org.dynamisengine.ai.cognition.CognitionService;
import org.dynamisengine.ai.cognition.DialogueRequest;
import org.dynamisengine.ai.cognition.DialogueResponse;
import org.dynamisengine.ai.cognition.BeliefDecayPolicy;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.EnvironmentState;
import org.dynamisengine.ai.core.GlobalFacts;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.core.WorldSnapshot;
import org.dynamisengine.ai.perception.PerceptionSnapshot;
import org.dynamisengine.ai.planning.HtnTask;
import org.dynamisengine.ai.planning.Plan;
import org.dynamisengine.scripting.api.value.CanonTime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class AffectiveStateDebuggerTest {

    @Test
    void captureWithNullPlanUsesNoneAndZeroConfidence() {
        EntityId agent = EntityId.of(1);
        AffectiveStateDebugger d = new AffectiveStateDebugger(new StubCognitionService());

        NpcDebugSnapshot s = d.capture(agent, world(agent), emptyPerception(agent), null,
            AffectVector.neutral(), 0.4f, 1L);

        assertEquals("none", s.activeHtnGoal());
        assertEquals(0f, s.decisionTrace().confidence(), 1e-6f);
    }

    @Test
    void captureWithPlanExtractsGoalAndAction() {
        EntityId agent = EntityId.of(1);
        AffectiveStateDebugger d = new AffectiveStateDebugger(new StubCognitionService());
        Plan plan = new Plan(
            List.of(new HtnTask.PrimitiveTask("act", "desc", ws -> true, List.of(), 0.1f, () -> {})),
            0.1f, 1, true);

        NpcDebugSnapshot s = d.capture(agent, world(agent), emptyPerception(agent), plan,
            AffectVector.neutral(), 0.4f, 1L);

        assertEquals("act", s.decisionTrace().currentAction());
        assertEquals("act", s.activeHtnGoal());
    }

    @Test
    void captureWithEmptyPerceptionHasEmptyOverlay() {
        EntityId agent = EntityId.of(1);
        AffectiveStateDebugger d = new AffectiveStateDebugger(new StubCognitionService());
        NpcDebugSnapshot s = d.capture(agent, world(agent), emptyPerception(agent), null,
            AffectVector.neutral(), 0.4f, 1L);
        assertTrue(s.perceptionOverlay().isEmpty());
    }

    @Test
    void captureAllReturnsBothAgents() {
        AffectiveStateDebugger d = new AffectiveStateDebugger(new StubCognitionService());
        EntityId a = EntityId.of(1);
        EntityId b = EntityId.of(2);

        Map<EntityId, NpcDebugSnapshot> map = d.captureAll(
            List.of(a, b),
            world(a),
            Map.of(a, emptyPerception(a), b, emptyPerception(b)),
            Map.of(),
            Map.of(a, AffectVector.neutral(), b, AffectVector.neutral()),
            Map.of(a, 0.2f, b, 0.3f),
            1L
        );

        assertEquals(2, map.size());
        assertTrue(map.containsKey(a));
        assertTrue(map.containsKey(b));
    }

    @Test
    void formatContainsAgentTickAndAffectValues() {
        EntityId a = EntityId.of(1);
        NpcDebugSnapshot s = new NpcDebugSnapshot(
            a, 10L,
            new AffectRadarSnapshot(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 10L),
            new DecisionTraceEntry("g", "p", "a", 0.9f, List.of()),
            List.of(), "goal", Map.of(), List.of()
        );

        String out = AffectiveStateDebugger.format(s);
        assertTrue(out.contains(String.valueOf(a)));
        assertTrue(out.contains("tick [10]"));
        assertTrue(out.contains("fear=0.10"));
    }

    @Test
    void formatWithEmptyBeliefsDoesNotThrow() {
        EntityId a = EntityId.of(1);
        NpcDebugSnapshot s = new NpcDebugSnapshot(
            a, 10L,
            new AffectRadarSnapshot(0, 0, 0, 0, 0, 10L),
            new DecisionTraceEntry("g", "p", "a", 0.9f, List.of()),
            List.of(), "goal", Map.of(), List.of()
        );
        assertDoesNotThrow(() -> AffectiveStateDebugger.format(s));
    }

    private static WorldSnapshot world(EntityId agent) {
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(agent, new EntityState(agent, new Location(0, 0, 0), Map.of("flag.alpha", true, "state", "on")));
        return new WorldSnapshot(1L, entities,
            new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }

    private static PerceptionSnapshot emptyPerception(EntityId agent) {
        return new PerceptionSnapshot(agent, 1L, List.of(), Optional.empty(), ThreatLevel.NONE, new Location(0, 0, 0), 0);
    }

    private static final class StubCognitionService implements CognitionService {
        private final BeliefModelRegistry registry = new BeliefModelRegistry(BeliefDecayPolicy.defaultPolicy());

        StubCognitionService() {
            BeliefModel model = registry.getOrCreate(EntityId.of(1));
            model.assertBelief("entity.player.visible", true, 0.9f, 1L);
            model.assertBelief("entity.player.audible", true, 0.6f, 1L);
        }

        @Override public CompletableFuture<DialogueResponse> requestDialogue(DialogueRequest request) { return CompletableFuture.completedFuture(null); }
        @Override public CompletableFuture<DialogueResponse> inferDeterministic(DialogueRequest request, long seed) { return CompletableFuture.completedFuture(null); }
        @Override public DialogueResponse getFallback(EntityId speaker) { return null; }
        @Override public void warmCache(EntityId speaker, DialogueResponse response) {}
        @Override public int getQueueDepth() { return 0; }
        @Override public BeliefModel beliefsFor(EntityId entityId) { return registry.getOrCreate(entityId); }
        @Override public BeliefModelRegistry beliefRegistry() { return registry; }
        @Override public void setCanonTimeSource(Supplier<CanonTime> source) {}
        @Override public void shutdown() {}
    }
}
