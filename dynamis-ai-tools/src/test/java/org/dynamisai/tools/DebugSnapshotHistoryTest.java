package org.dynamisai.tools;

import org.dynamis.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DebugSnapshotHistoryTest {

    @Test
    void recordThenHistoryForReturnsSnapshot() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(5);
        NpcDebugSnapshot s = snap(EntityId.of(1), 1);
        h.record(s);
        assertEquals(1, h.historyFor(EntityId.of(1)).size());
    }

    @Test
    void atTickReturnsExactMatch() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(5);
        h.record(snap(EntityId.of(1), 2));
        assertTrue(h.atTick(EntityId.of(1), 2).isPresent());
    }

    @Test
    void atTickAbsentReturnsEmpty() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(5);
        h.record(snap(EntityId.of(1), 2));
        assertTrue(h.atTick(EntityId.of(1), 3).isEmpty());
    }

    @Test
    void boundedHistoryDropsOldest() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(3);
        EntityId a = EntityId.of(1);
        h.record(snap(a, 1));
        h.record(snap(a, 2));
        h.record(snap(a, 3));
        h.record(snap(a, 4));

        List<NpcDebugSnapshot> list = h.historyFor(a);
        assertEquals(3, list.size());
        assertEquals(2, list.get(0).tick());
    }

    @Test
    void pruneOlderThanRemovesOldEntries() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(10);
        EntityId a = EntityId.of(1);
        h.record(snap(a, 1));
        h.record(snap(a, 5));
        h.pruneOlderThan(5);
        assertEquals(1, h.snapshotCount(a));
        assertEquals(5, h.historyFor(a).get(0).tick());
    }

    @Test
    void snapshotCountAccurate() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(10);
        EntityId a = EntityId.of(1);
        h.record(snap(a, 1));
        h.record(snap(a, 2));
        assertEquals(2, h.snapshotCount(a));
    }

    @Test
    void multipleAgentsIndependent() {
        DebugSnapshotHistory h = new DebugSnapshotHistory(10);
        h.record(snap(EntityId.of(1), 1));
        h.record(snap(EntityId.of(2), 1));
        assertEquals(1, h.snapshotCount(EntityId.of(1)));
        assertEquals(1, h.snapshotCount(EntityId.of(2)));
    }

    private static NpcDebugSnapshot snap(EntityId id, long tick) {
        return new NpcDebugSnapshot(
            id,
            tick,
            new AffectRadarSnapshot(0, 0, 0, 0, 0, tick),
            new DecisionTraceEntry("g", "p", "a", 1f, List.of()),
            List.of(),
            "goal",
            Map.of(),
            List.of()
        );
    }
}
