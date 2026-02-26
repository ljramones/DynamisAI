package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UtilityEvaluatorTest {

    @Test
    void singleActionSelected() {
        UtilityEvaluator eval = new UtilityEvaluator();
        eval.register(new UtilityAction("idle", s -> 0.5, null));

        Optional<UtilityAction> selected = eval.selectAndExecute(
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()));

        assertTrue(selected.isPresent());
        assertEquals("idle", selected.get().name());
    }

    @Test
    void higherScoreWins() {
        UtilityEvaluator eval = new UtilityEvaluator();
        eval.register(new UtilityAction("low", s -> 0.2, null));
        eval.register(new UtilityAction("high", s -> 0.9, null));

        UtilityAction winner = eval.selectAndExecute(
            PlanningTestFixtures.state(EntityId.of(1L), Map.of())).orElseThrow();
        assertEquals("high", winner.name());
    }

    @Test
    void onSelectedCallbackFires() {
        UtilityEvaluator eval = new UtilityEvaluator();
        AtomicInteger counter = new AtomicInteger();
        eval.register(new UtilityAction("act", s -> 1.0, counter::incrementAndGet));

        eval.selectAndExecute(PlanningTestFixtures.state(EntityId.of(1L), Map.of()));
        assertEquals(1, counter.get());
    }

    @Test
    void scoresAreClampedToRange() {
        UtilityAction tooLow = new UtilityAction("low", s -> -10.0, null);
        UtilityAction tooHigh = new UtilityAction("high", s -> 10.0, null);
        WorldState state = PlanningTestFixtures.state(EntityId.of(1L), Map.of());

        assertEquals(0.0, tooLow.score(state), 1e-9);
        assertEquals(1.0, tooHigh.score(state), 1e-9);
    }

    @Test
    void emptyEvaluatorReturnsEmpty() {
        UtilityEvaluator eval = new UtilityEvaluator();
        assertTrue(eval.selectAndExecute(
            PlanningTestFixtures.state(EntityId.of(1L), Map.of())).isEmpty());
    }

    @Test
    void evaluateReturnsDescendingOrder() {
        UtilityEvaluator eval = new UtilityEvaluator();
        eval.register(new UtilityAction("b", s -> 0.4, null));
        eval.register(new UtilityAction("a", s -> 0.8, null));
        eval.register(new UtilityAction("c", s -> 0.2, null));

        List<ScoredAction> list = eval.evaluate(
            PlanningTestFixtures.state(EntityId.of(1L), Map.of()));

        assertEquals("a", list.get(0).action().name());
        assertEquals("b", list.get(1).action().name());
        assertEquals("c", list.get(2).action().name());
    }
}
