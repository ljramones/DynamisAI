package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NpcDebugSnapshotTest {

    @Test
    void constructionAndAccess() {
        NpcDebugSnapshot s = new NpcDebugSnapshot(
            EntityId.of(1),
            10L,
            new AffectRadarSnapshot(0, 0, 0, 0, 0, 10),
            new DecisionTraceEntry("g", "p", "a", 1f, List.of("b")),
            List.of(new PerceptionOverlayEntry(EntityId.of(2), "VISUAL", 0.8f, true, new Location(1, 0, 1))),
            "goal",
            Map.of("x", 0.7f),
            List.of("flag")
        );
        assertEquals(EntityId.of(1), s.agent());
        assertEquals(0.7f, s.beliefSummary().get("x"), 1e-6f);
    }
}
