package org.dynamisai.tools;

import io.vavr.collection.HashMap;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.Location;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotStoreTest {

    @Test
    void recordAndAtTick() {
        SnapshotStore store = new SnapshotStore(10);
        store.record(snapshot(1));
        assertTrue(store.atTick(1).isPresent());
        assertEquals(1L, store.atTick(1).get().tick());
    }

    @Test
    void capacityDropsOldest() {
        SnapshotStore store = new SnapshotStore(2);
        store.record(snapshot(1));
        store.record(snapshot(2));
        store.record(snapshot(3));
        assertFalse(store.atTick(1).isPresent());
        assertEquals(2, store.size());
    }

    @Test
    void recentReturnsDescending() {
        SnapshotStore store = new SnapshotStore(10);
        store.record(snapshot(1));
        store.record(snapshot(2));
        store.record(snapshot(3));
        assertEquals(3L, store.recent(2).getFirst().tick());
        assertEquals(2L, store.recent(2).get(1).tick());
    }

    @Test
    void oldestAndNewestTickReported() {
        SnapshotStore store = new SnapshotStore(10);
        assertEquals(-1L, store.oldestTick());
        assertEquals(-1L, store.newestTick());

        store.record(snapshot(7));
        store.record(snapshot(9));
        assertEquals(7L, store.oldestTick());
        assertEquals(9L, store.newestTick());
    }

    @Test
    void exportAndImportRoundTrip() {
        SnapshotStore store = new SnapshotStore(10);
        store.record(snapshot(1));
        store.record(snapshot(2));

        String jsonl = store.exportJsonLines();
        SnapshotStore loaded = new SnapshotStore(10);
        loaded.importJsonLines(jsonl);

        assertEquals(2, loaded.size());
        assertTrue(loaded.atTick(1).isPresent());
        assertTrue(loaded.atTick(2).isPresent());
    }

    @Test
    void malformedImportLinesAreSkipped() {
        SnapshotStore store = new SnapshotStore(10);
        store.importJsonLines("bad-line\n{\"tick\":2,\"seed\":3,\"entityCount\":1,\"recordedAt\":7}\n{}");
        assertEquals(1, store.size());
        assertTrue(store.atTick(2).isPresent());
    }

    @Test
    void recentOnEmptyIsEmpty() {
        SnapshotStore store = new SnapshotStore(10);
        assertTrue(store.recent(5).isEmpty());
        assertEquals(-1L, store.oldestTick());
        assertEquals(-1L, store.newestTick());
    }

    private static WorldSnapshot snapshot(long tick) {
        EntityId id = EntityId.of(tick);
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(id, new EntityState(id, new Location(tick, 0, tick), Map.of("tick", tick)));
        return new WorldSnapshot(tick, entities, new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }
}
