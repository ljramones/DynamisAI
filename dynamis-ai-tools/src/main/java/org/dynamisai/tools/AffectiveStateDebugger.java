package org.dynamisai.tools;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.Belief;
import org.dynamisai.cognition.BeliefModel;
import org.dynamisai.cognition.CognitionService;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.WorldSnapshot;
import org.dynamisai.perception.Percept;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.planning.Plan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AffectiveStateDebugger {

    private final CognitionService cognitionService;

    public AffectiveStateDebugger(CognitionService cognitionService) {
        this.cognitionService = Objects.requireNonNull(cognitionService);
    }

    public NpcDebugSnapshot capture(EntityId agent,
                                    WorldSnapshot worldSnapshot,
                                    PerceptionSnapshot perceptionSnapshot,
                                    Plan currentPlan,
                                    AffectVector currentAffect,
                                    float loyaltyUrgency,
                                    long tick) {
        AffectRadarSnapshot radar = AffectRadarSnapshot.from(currentAffect, loyaltyUrgency, tick);

        BeliefModel beliefs = cognitionService.beliefsFor(agent);
        List<Belief> sortedBeliefs = beliefs.allBeliefs().stream()
            .sorted(Comparator.comparingDouble(Belief::confidence).reversed())
            .toList();

        List<String> evidence = sortedBeliefs.stream()
            .limit(3)
            .map(Belief::key)
            .toList();

        DecisionTraceEntry trace = decisionTrace(currentPlan, evidence);

        List<PerceptionOverlayEntry> overlay = new ArrayList<>();
        if (perceptionSnapshot != null) {
            for (Percept p : perceptionSnapshot.percepts()) {
                overlay.add(new PerceptionOverlayEntry(
                    p.source(),
                    p.stimulusType().name(),
                    p.rawIntensity(),
                    true,
                    p.location()
                ));
            }
        }

        String activeGoal = (currentPlan != null && currentPlan.hasTasks())
            ? currentPlan.tasks().get(0).taskId()
            : "none";

        Map<String, Float> beliefSummary = new LinkedHashMap<>();
        for (Belief belief : sortedBeliefs.stream().limit(5).toList()) {
            beliefSummary.put(belief.key(), belief.confidence());
        }

        List<String> activeFlags = activeFlags(agent, worldSnapshot);

        return new NpcDebugSnapshot(
            agent,
            tick,
            radar,
            trace,
            List.copyOf(overlay),
            activeGoal,
            Map.copyOf(beliefSummary),
            List.copyOf(activeFlags)
        );
    }

    public Map<EntityId, NpcDebugSnapshot> captureAll(
        List<EntityId> agents,
        WorldSnapshot worldSnapshot,
        Map<EntityId, PerceptionSnapshot> perceptions,
        Map<EntityId, Plan> plans,
        Map<EntityId, AffectVector> affects,
        Map<EntityId, Float> loyalties,
        long tick
    ) {
        Map<EntityId, NpcDebugSnapshot> out = new LinkedHashMap<>();
        for (EntityId agent : agents) {
            out.put(agent, capture(
                agent,
                worldSnapshot,
                perceptions != null ? perceptions.get(agent) : null,
                plans != null ? plans.get(agent) : null,
                affects != null ? affects.get(agent) : AffectVector.neutral(),
                loyalties != null ? loyalties.getOrDefault(agent, 0f) : 0f,
                tick
            ));
        }
        return Map.copyOf(out);
    }

    public static String format(NpcDebugSnapshot snapshot) {
        if (snapshot == null) {
            return "=== NPC [unknown] @ tick [0] ===\nAFFECT: fear=0.00 suspicion=0.00 curiosity=0.00 aggression=0.00 loyalty=0.00\nGOAL: none\nACTION: none (confidence: 0.00)\nBELIEFS: none\nPERCEPTS: 0 visible, 0 auditory";
        }

        AffectRadarSnapshot a = snapshot.affectRadar();
        DecisionTraceEntry d = snapshot.decisionTrace();
        long visual = snapshot.perceptionOverlay().stream()
            .filter(p -> "VISUAL".equals(p.stimulusType()))
            .count();
        long auditory = snapshot.perceptionOverlay().stream()
            .filter(p -> "AUDITORY".equals(p.stimulusType()))
            .count();

        String beliefs = snapshot.beliefSummary().isEmpty()
            ? "none"
            : snapshot.beliefSummary().entrySet().stream()
                .map(e -> e.getKey() + "=" + String.format("%.2f", e.getValue()))
                .reduce((x, y) -> x + ", " + y)
                .orElse("none");

        return String.format(
            "=== NPC [%s] @ tick [%d] ===%n" +
                "AFFECT: fear=%.2f suspicion=%.2f curiosity=%.2f aggression=%.2f loyalty=%.2f%n" +
                "GOAL: %s%n" +
                "ACTION: %s (confidence: %.2f)%n" +
                "BELIEFS: %s%n" +
                "PERCEPTS: %d visible, %d auditory",
            snapshot.agent(), snapshot.tick(),
            a != null ? a.fear() : 0f,
            a != null ? a.suspicion() : 0f,
            a != null ? a.curiosity() : 0f,
            a != null ? a.aggression() : 0f,
            a != null ? a.loyalty() : 0f,
            snapshot.activeHtnGoal() != null ? snapshot.activeHtnGoal() : "none",
            d != null ? d.currentAction() : "none",
            d != null ? d.confidence() : 0f,
            beliefs,
            visual,
            auditory
        );
    }

    private static DecisionTraceEntry decisionTrace(Plan plan, List<String> evidence) {
        if (plan == null || !plan.hasTasks()) {
            return new DecisionTraceEntry("none", "none", "none", 0f, evidence);
        }
        String current = plan.tasks().get(0).taskId();
        return new DecisionTraceEntry(
            "htn-goal",
            "htn-plan",
            current,
            1f,
            evidence
        );
    }

    private static List<String> activeFlags(EntityId agent, WorldSnapshot snapshot) {
        if (snapshot == null) {
            return List.of();
        }

        EntityState state = snapshot.entities().get(agent).getOrNull();
        if (state == null || state.properties() == null) {
            return List.of();
        }

        List<String> flags = new ArrayList<>();
        for (Map.Entry<String, Object> e : state.properties().entrySet()) {
            Object value = e.getValue();
            if (Boolean.TRUE.equals(value) || value instanceof String s && !s.isBlank()) {
                flags.add(e.getKey());
            }
        }
        return List.copyOf(flags);
    }
}
