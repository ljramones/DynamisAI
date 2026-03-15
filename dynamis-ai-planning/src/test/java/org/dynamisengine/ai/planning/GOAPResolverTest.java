package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GOAPResolverTest {

    @Test
    void singleActionSatisfiesGoal() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("set-done", s -> true,
            List.of(new PlannerEffect("done", true)), 0.2f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);

        Optional<Plan> plan = resolver.resolve(goal, state, PlanningBudget.standard());

        assertTrue(plan.isPresent());
        assertEquals(1, plan.get().tasks().size());
        assertEquals("set-done", plan.get().tasks().get(0).taskId());
        assertTrue(plan.get().isComplete());
    }

    @Test
    void noApplicableActionReturnsEmpty() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("never", s -> false,
            List.of(new PlannerEffect("done", true)), 0.2f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);

        assertTrue(resolver.resolve(goal, state, PlanningBudget.standard()).isEmpty());
    }

    @Test
    void twoStepPlanProducesCorrectSequence() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("get-key", s -> true,
            List.of(new PlannerEffect("has.key", true)), 0.3f));
        lib.register(GoapAction.of("open-door", s -> s.is("has.key", true),
            List.of(new PlannerEffect("door.open", true)), 0.4f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("door", s -> s.is("door.open", true), 1f);

        Plan plan = resolver.resolve(goal, state, PlanningBudget.standard()).orElseThrow();

        assertEquals(2, plan.tasks().size());
        assertEquals("get-key", plan.tasks().get(0).taskId());
        assertEquals("open-door", plan.tasks().get(1).taskId());
    }

    @Test
    void budgetExceededReturnsPartialPlan() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("step-1", s -> true,
            List.of(new PlannerEffect("a", true)), 0.1f));
        lib.register(GoapAction.of("step-2", s -> s.is("a", true),
            List.of(new PlannerEffect("b", true)), 0.1f));
        lib.register(GoapAction.of("step-3", s -> s.is("b", true),
            List.of(new PlannerEffect("done", true)), 0.1f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);

        Plan plan = resolver.resolve(goal, state, new PlanningBudget(8, 1, 5L)).orElseThrow();

        assertFalse(plan.isComplete());
    }

    @Test
    void lowerCostPathPreferred() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("cheap", s -> true,
            List.of(new PlannerEffect("done", true)), 0.2f));
        lib.register(GoapAction.of("expensive", s -> true,
            List.of(new PlannerEffect("done", true)), 1.0f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);

        Plan plan = resolver.resolve(goal, state, PlanningBudget.standard()).orElseThrow();

        assertEquals("cheap", plan.tasks().get(0).taskId());
        assertEquals(0.2f, plan.totalCost(), 1e-6f);
    }

    @Test
    void resolveHighestPriorityReturnsHighestSatisfiableGoal() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("make-high", s -> true,
            List.of(new PlannerEffect("high", true)), 0.1f));
        lib.register(GoapAction.of("make-low", s -> true,
            List.of(new PlannerEffect("low", true)), 0.1f));

        GOAPResolver resolver = new GOAPResolver(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        GoapGoal low = new GoapGoal("low", s -> s.is("low", true), 0.4f);
        GoapGoal high = new GoapGoal("high", s -> s.is("high", true), 0.9f);

        Plan plan = resolver.resolveHighestPriority(List.of(low, high), state,
            PlanningBudget.standard()).orElseThrow();
        assertEquals("make-high", plan.tasks().get(0).taskId());
    }
}
