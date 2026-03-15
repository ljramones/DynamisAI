package org.dynamisengine.ai.tools;

import io.vavr.collection.HashMap;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.EnvironmentState;
import org.dynamisengine.ai.core.GlobalFacts;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplaySessionTest {

    @Test
    void seekToExactTickReturnsExact() {
        Fixture f = fixture();
        assertEquals(3L, f.session.seekTo(3).orElseThrow().tick());
    }

    @Test
    void seekBetweenTicksReturnsClosestEarlier() {
        Fixture f = fixture();
        assertEquals(3L, f.session.seekTo(4).orElseThrow().tick());
    }

    @Test
    void seekBeforeOldestReturnsOldest() {
        Fixture f = fixture();
        assertEquals(1L, f.session.seekTo(0).orElseThrow().tick());
    }

    @Test
    void stepForwardAndBackward() {
        Fixture f = fixture();
        f.session.seekTo(2);
        assertEquals(3L, f.session.stepForward().orElseThrow().tick());
        assertEquals(2L, f.session.stepBackward().orElseThrow().tick());
    }

    @Test
    void steppingAtBoundsReturnsEmpty() {
        Fixture f = fixture();
        f.session.seekTo(3);
        assertTrue(f.session.stepForward().isEmpty());
        f.session.seekTo(1);
        assertTrue(f.session.stepBackward().isEmpty());
    }

    @Test
    void replayStateFlagsWork() {
        Fixture f = fixture();
        assertFalse(f.session.isReplaying());
        f.session.seekTo(2);
        assertTrue(f.session.isReplaying());
        f.session.exitReplay();
        assertFalse(f.session.isReplaying());
        assertEquals(-1L, f.session.currentTick());
    }

    @Test
    void debugSnapshotsAtCurrentTickUsesHistory() {
        Fixture f = fixture();
        f.session.seekTo(2);
        assertEquals(1, f.session.debugSnapshotsAtCurrentTick().size());
        f.session.exitReplay();
        assertTrue(f.session.debugSnapshotsAtCurrentTick().isEmpty());
    }

    private static Fixture fixture() {
        SnapshotStore store = new SnapshotStore(10);
        DebugSnapshotHistory history = new DebugSnapshotHistory(10);

        store.record(snapshot(1));
        store.record(snapshot(2));
        store.record(snapshot(3));

        EntityId id = EntityId.of(1);
        history.record(new NpcDebugSnapshot(
            id,
            2,
            new AffectRadarSnapshot(0, 0, 0, 0, 0, 2),
            new DecisionTraceEntry("goal", "plan", "action", 1f, java.util.List.of()),
            java.util.List.of(),
            "goal",
            java.util.Map.of(),
            java.util.List.of()));

        return new Fixture(new ReplaySession(store, history));
    }

    private static WorldSnapshot snapshot(long tick) {
        EntityId id = EntityId.of(tick);
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(id, new EntityState(id, new Location(tick, 0, tick), Map.of("tick", tick)));
        return new WorldSnapshot(tick, entities, new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }

    private record Fixture(ReplaySession session) {}
}
