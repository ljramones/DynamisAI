package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GoapGoalTest {

    @Test
    void predicateEvaluatesCorrectly() {
        WorldState s = PlanningTestFixtures.state(EntityId.of(1L), Map.of("done", true));
        GoapGoal goal = new GoapGoal("done", st -> st.is("done", true), 0.5f);
        assertTrue(goal.isSatisfied().test(s));
    }

    @Test
    void priorityOutsideRangeRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new GoapGoal("x", st -> true, -0.1f));
        assertThrows(IllegalArgumentException.class,
            () -> new GoapGoal("x", st -> true, 1.1f));
    }
}
