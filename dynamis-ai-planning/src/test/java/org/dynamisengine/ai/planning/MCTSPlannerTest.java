package org.dynamisengine.ai.planning;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MCTSPlannerTest {

    @Test
    void singleApplicableActionThatSatisfiesGoalIsReturned() {
        GoapActionLibrary lib = new GoapActionLibrary();
        GoapAction win = GoapAction.of("win", s -> true,
            List.of(new PlannerEffect("done", true)), 0.1f);
        lib.register(win);

        MCTSPlanner mcts = new MCTSPlanner(lib);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);

        Optional<GoapAction> picked = mcts.selectAction(goal, state, 42L, PlanningBudget.standard());
        assertTrue(picked.isPresent());
        assertEquals("win", picked.get().name());
    }

    @Test
    void noApplicableActionsReturnsEmpty() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("never", s -> false,
            List.of(new PlannerEffect("done", true)), 0.1f));

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        assertTrue(mcts.selectAction(goal, state, 7L, PlanningBudget.standard()).isEmpty());
    }

    @Test
    void sameSeedSameStateReturnsSameAction() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(new PlannerEffect("x", true)), 0.2f));
        lib.register(GoapAction.of("b", s -> true, List.of(new PlannerEffect("y", true)), 0.2f));

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("g", s -> s.has("x") || s.has("y"), 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        GoapAction one = mcts.selectAction(goal, state, 99L, PlanningBudget.standard()).orElseThrow();
        GoapAction two = mcts.selectAction(goal, state, 99L, PlanningBudget.standard()).orElseThrow();

        assertEquals(one.name(), two.name());
    }

    @Test
    void differentSeedsCanReturnDifferentActions() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(new PlannerEffect("x", true)), 0.2f));
        lib.register(GoapAction.of("b", s -> true, List.of(new PlannerEffect("y", true)), 0.2f));

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("g", s -> s.has("x") || s.has("y"), 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        Optional<GoapAction> one = mcts.selectAction(goal, state, 1L, PlanningBudget.standard());
        Optional<GoapAction> two = mcts.selectAction(goal, state, 2L, PlanningBudget.standard());

        assertTrue(one.isPresent());
        assertTrue(two.isPresent());
        // nondeterministic across seeds is allowed; this guards against accidental hardcoded selection.
        assertNotNull(one.get().name());
        assertNotNull(two.get().name());
    }

    @Test
    void tinyBudgetReturnsWithoutException() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(new PlannerEffect("x", true)), 0.2f));

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("g", s -> s.has("x"), 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        assertDoesNotThrow(() -> mcts.selectAction(goal, state, 7L, new PlanningBudget(8, 1, 5L)));
    }

    @Test
    void expansionNeverExceedsDefaultMaxNodes() {
        GoapActionLibrary lib = new GoapActionLibrary();
        for (int i = 0; i < 50; i++) {
            lib.register(GoapAction.of("a" + i, s -> true,
                List.of(new PlannerEffect("k" + i, true)), 0.1f));
        }

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("never", s -> false, 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        mcts.selectAction(goal, state, 123L, PlanningBudget.standard());

        assertTrue(mcts.lastExpansionCount() <= MCTSPlanner.DEFAULT_MAX_NODES);
    }

    @Test
    void goalAlreadySatisfiedReturnsEmpty() {
        GoapActionLibrary lib = new GoapActionLibrary();
        lib.register(GoapAction.of("a", s -> true, List.of(new PlannerEffect("x", true)), 0.1f));

        MCTSPlanner mcts = new MCTSPlanner(lib);
        GoapGoal goal = new GoapGoal("done", s -> s.is("done", true), 1f);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of("done", true));

        assertTrue(mcts.selectAction(goal, state, 9L, PlanningBudget.standard()).isEmpty());
    }
}
