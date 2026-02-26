package org.dynamisai.planning;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.perception.PerceptionSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HtnPlannerTest {

    private DefaultHtnPlanner planner;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        planner = new DefaultHtnPlanner();
        npc = EntityId.of(1L);
    }

    private WorldState worldState(ThreatLevel threat, Map<String, Object> blackboard) {
        PerceptionSnapshot emptyPerception =
            PerceptionSnapshot.empty(npc, 0L, new Location(0, 0, 0));
        MemoryStats stats = new MemoryStats(npc, 0, 0, 0, 0, 0);
        return WorldState.withoutNav(npc, 0L, AffectVector.neutral(),
            threat, emptyPerception, stats, blackboard);
    }

    private WorldState withPercept(ThreatLevel threat, Map<String, Object> blackboard) {
        PerceptionSnapshot snap = new PerceptionSnapshot(
            npc, 0L,
            List.of(new org.dynamisai.perception.Percept(
                EntityId.of(2L),
                org.dynamisai.perception.StimulusType.VISUAL,
                new Location(5, 0, 0), 0.7f, 0.7f, threat, false)),
            Optional.empty(),
            threat, new Location(0, 0, 0), 1
        );
        MemoryStats stats = new MemoryStats(npc, 0, 0, 0, 0, 0);
        return WorldState.withoutNav(npc, 0L, AffectVector.neutral(),
            threat, snap, stats, blackboard);
    }

    @Test
    void primitiveTaskProducesSingleStepPlan() {
        HtnTask.PrimitiveTask task = TaskLibrary.idleTask(() -> {});
        WorldState state = worldState(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(task, state, PlanningBudget.standard());
        assertTrue(plan.hasTasks());
        assertEquals("idle", plan.tasks().get(0).taskId());
    }

    @Test
    void primitiveTaskWithFailedPreconditionProducesEmptyPlan() {
        HtnTask.PrimitiveTask flee = TaskLibrary.fleeThreatTask(() -> {});
        WorldState state = worldState(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(flee, state, PlanningBudget.standard());
        assertFalse(plan.hasTasks());
    }

    @Test
    void compoundTaskSelectsFirstApplicableMethod() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        WorldState highThreat = worldState(ThreatLevel.HIGH, Map.of());
        HtnTask.CompoundTask survive = TaskLibrary.surviveTask(flee, idle, dialogue);
        Plan plan = planner.plan(survive, highThreat, PlanningBudget.standard());

        assertTrue(plan.hasTasks());
        assertEquals("flee-threat", plan.tasks().get(0).taskId());
    }

    @Test
    void compoundTaskFallsBackToIdleWhenNoThreatNoPercept() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        WorldState calm = worldState(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(
            TaskLibrary.surviveTask(flee, idle, dialogue), calm, PlanningBudget.standard());

        assertTrue(plan.hasTasks());
        assertEquals("idle", plan.tasks().get(0).taskId());
    }

    @Test
    void compoundTaskSelectsSocialApproachWhenPlayerVisible() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        WorldState playerVisible = withPercept(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(
            TaskLibrary.surviveTask(flee, idle, dialogue), playerVisible,
            PlanningBudget.standard());

        assertTrue(plan.hasTasks());
        assertEquals("approach-player", plan.tasks().get(0).taskId());
    }

    @Test
    void planEffectsAreAppliedBetweenSubtasks() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        WorldState playerVisible = withPercept(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(
            TaskLibrary.surviveTask(flee, idle, dialogue),
            playerVisible, PlanningBudget.standard());

        assertEquals(2, plan.tasks().size());
        assertEquals("approach-player", plan.tasks().get(0).taskId());
        assertEquals("initiate-dialogue", plan.tasks().get(1).taskId());
    }

    @Test
    void conditionalTaskSelectsCorrectBranch() {
        HtnTask.PrimitiveTask idleTask = TaskLibrary.idleTask(() -> {});
        HtnTask.PrimitiveTask fleeTask = TaskLibrary.fleeThreatTask(() -> {});

        HtnTask.ConditionalTask conditional = new HtnTask.ConditionalTask(
            "threat-check",
            state -> state.currentThreat().ordinal() >= ThreatLevel.HIGH.ordinal(),
            fleeTask,
            idleTask
        );

        Plan highPlan = planner.plan(conditional,
            worldState(ThreatLevel.HIGH, Map.of()), PlanningBudget.standard());
        Plan nonePlan = planner.plan(conditional,
            worldState(ThreatLevel.NONE, Map.of()), PlanningBudget.standard());

        assertEquals("flee-threat", highPlan.tasks().get(0).taskId());
        assertEquals("idle", nonePlan.tasks().get(0).taskId());
    }

    @Test
    void plannerRespectsNodeCapBudget() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        PlanningBudget tiny = new PlanningBudget(8, 1, 50L);
        WorldState calm = worldState(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(
            TaskLibrary.surviveTask(flee, idle, dialogue), calm, tiny);

        assertFalse(plan.isComplete());
    }

    @Test
    void plannerRespectsDepthLimit() {
        PlanningBudget depthOne = new PlanningBudget(1, 64, 50L);
        HtnTask.CompoundTask compound = new HtnTask.CompoundTask(
            "compound", "test",
            List.of(new DecompositionMethod("m", s -> true,
                List.of(TaskLibrary.idleTask(() -> {}))))
        );
        WorldState calm = worldState(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(compound, calm, depthOne);
        assertFalse(plan.isComplete());
    }

    @Test
    void planTotalCostIsSum() {
        AtomicInteger flee = new AtomicInteger();
        AtomicInteger idle = new AtomicInteger();
        AtomicInteger dialogue = new AtomicInteger();

        WorldState playerVisible = withPercept(ThreatLevel.NONE, Map.of());
        Plan plan = planner.plan(
            TaskLibrary.surviveTask(flee, idle, dialogue),
            playerVisible, PlanningBudget.standard());

        assertEquals(0.8f, plan.totalCost(), 0.001f);
    }

    @Test
    void planIsImmutable() {
        Plan plan = planner.plan(TaskLibrary.idleTask(() -> {}),
            worldState(ThreatLevel.NONE, Map.of()), PlanningBudget.standard());
        assertThrows(UnsupportedOperationException.class,
            () -> plan.tasks().add(null));
    }

    @Test
    void planEmptyFactory() {
        Plan empty = Plan.empty();
        assertFalse(empty.hasTasks());
        assertEquals(0f, empty.totalCost());
        assertTrue(empty.isComplete());
    }

    @Test
    void worldStateBlackboardAccessors() {
        WorldState state = worldState(ThreatLevel.NONE, Map.of("key", "value"));
        assertTrue(state.has("key"));
        assertEquals("value", state.get("key"));
        assertTrue(state.is("key", "value"));
        assertFalse(state.has("missing"));
    }

    @Test
    void patternMatchOnTaskHierarchyIsExhaustive() {
        List<HtnTask> tasks = List.of(
            TaskLibrary.idleTask(() -> {}),
            new HtnTask.CompoundTask("c", "desc", List.of()),
            new HtnTask.ConditionalTask("cond", s -> true,
                TaskLibrary.idleTask(() -> {}), TaskLibrary.idleTask(() -> {}))
        );
        for (HtnTask task : tasks) {
            String type = switch (task) {
                case HtnTask.PrimitiveTask t -> "primitive";
                case HtnTask.CompoundTask t -> "compound";
                case HtnTask.ConditionalTask t -> "conditional";
            };
            assertNotNull(type);
        }
    }
}
