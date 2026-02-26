package org.dynamisai.tools;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecisionTraceEntryTest {

    @Test
    void constructionAndAccess() {
        DecisionTraceEntry d = new DecisionTraceEntry("g", "p", "a", 0.8f, List.of("m1", "m2"));
        assertEquals("g", d.goal());
        assertEquals("a", d.currentAction());
        assertEquals(2, d.memoryEvidence().size());
    }
}
