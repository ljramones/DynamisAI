package org.dynamisengine.ai.tools;

import io.vavr.collection.HashMap;
import org.dynamisengine.ai.core.DynamisAiEngine;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.EnvironmentState;
import org.dynamisengine.ai.core.GameEngineContext;
import org.dynamisengine.ai.core.GlobalFacts;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIInspectorTest {

    @Test
    void recordStoresSnapshotAndDebugData() {
        AIInspector inspector = new AIInspector(10, 10);
        EntityId id = EntityId.of(1);

        inspector.record(snapshot(1), Map.of(id, debug(id, 1)));

        assertEquals(1, inspector.snapshotStore().size());
        assertEquals(1, inspector.debugHistory().snapshotCount(id));
    }

    @Test
    void replaySessionCanAccessStoredData() {
        AIInspector inspector = new AIInspector(10, 10);
        inspector.record(snapshot(1), Map.of());
        ReplaySession session = inspector.openReplay();

        assertTrue(session.seekTo(1).isPresent());
        assertEquals(1L, session.currentTick());
    }

    @Test
    void activeReplaySessionsTracksOpenAndClose() {
        AIInspector inspector = new AIInspector(10, 10);
        ReplaySession session = inspector.openReplay();
        assertEquals(1, inspector.activeReplaySessions());
        inspector.closeReplay(session);
        assertEquals(0, inspector.activeReplaySessions());
    }

    @Test
    void timelineReturnsNonEmptyAfterRecording() {
        AIInspector inspector = new AIInspector(10, 10);
        inspector.record(snapshot(1), Map.of());
        String timeline = inspector.timeline(5);
        assertFalse(timeline.isBlank());
        assertTrue(timeline.contains("tick=1"));
    }

    @Test
    void exportImportRoundTrip() {
        AIInspector source = new AIInspector(10, 10);
        source.record(snapshot(1), Map.of());
        source.record(snapshot(2), Map.of());

        AIInspector target = new AIInspector(10, 10);
        target.importSnapshots(source.exportSnapshots());

        assertEquals(source.snapshotStore().size(), target.snapshotStore().size());
    }

    @Test
    void attachAndDetachInspectorOnEngine() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        AIInspector inspector = new AIInspector(10, 10);

        engine.attachInspector(inspector);
        assertTrue(engine.inspector().isPresent());
        assertEquals(inspector, engine.inspector().orElseThrow());

        engine.detachInspector();
        assertTrue(engine.inspector().isEmpty());
    }

    @Test
    void engineTickWithAttachedInspectorIncrementsStore() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        AIInspector inspector = new AIInspector(10, 10);
        engine.attachInspector(inspector);

        engine.tick(GameEngineContext.builder(1L, 0.016f).build());
        assertEquals(1, inspector.snapshotStore().size());

        engine.detachInspector();
        engine.tick(GameEngineContext.builder(2L, 0.016f).build());
        assertEquals(1, inspector.snapshotStore().size());
    }

    private static WorldSnapshot snapshot(long tick) {
        EntityId id = EntityId.of(tick);
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(id, new EntityState(id, new Location(tick, 0, tick), Map.of("tick", tick)));
        return new WorldSnapshot(tick, entities, new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }

    private static NpcDebugSnapshot debug(EntityId id, long tick) {
        return new NpcDebugSnapshot(
            id,
            tick,
            new AffectRadarSnapshot(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, tick),
            new DecisionTraceEntry("goal", "plan", "action", 0.9f, List.of("belief.a")),
            List.of(),
            "goal",
            Map.of("belief.a", 0.9f),
            List.of("flag.a")
        );
    }
}
