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
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldSnapshotRecordTest {

    @Test
    void fromProducesTickAndSeed() {
        WorldSnapshot snapshot = snapshotWithEntity(5L);
        WorldSnapshotRecord record = WorldSnapshotRecord.from(snapshot);
        assertEquals(snapshot.tick(), record.tick());
        assertEquals(snapshot.deterministicSeed(), record.deterministicSeed());
    }

    @Test
    void entitiesMapContainsAllEntities() {
        EntityId a = EntityId.of(1L);
        EntityId b = EntityId.of(2L);
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(a, new EntityState(a, new Location(1, 0, 1), Map.of("hp", 10)))
            .put(b, new EntityState(b, new Location(2, 0, 2), Map.of("hp", 20)));
        WorldSnapshot snapshot = new WorldSnapshot(
            1L, entities, new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));

        WorldSnapshotRecord record = WorldSnapshotRecord.from(snapshot);
        assertEquals(2, record.entities().size());
        assertTrue(record.entities().containsKey(a.toString()));
        assertTrue(record.entities().containsKey(b.toString()));
    }

    @Test
    void summaryContainsTickAndEntityCount() {
        WorldSnapshotRecord record = WorldSnapshotRecord.from(snapshotWithEntity(9L));
        assertTrue(record.summary().contains("tick=9"));
        assertTrue(record.summary().contains("entities=1"));
    }

    @Test
    void fromWithEmptySnapshotWorks() {
        WorldSnapshot snapshot = new WorldSnapshot(
            1L,
            HashMap.empty(),
            new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 12f, 1f)
        );
        WorldSnapshotRecord record = WorldSnapshotRecord.from(snapshot);
        assertEquals(0, record.entities().size());
    }

    private static WorldSnapshot snapshotWithEntity(long tick) {
        EntityId id = EntityId.of(1L);
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(id, new EntityState(id, new Location(3, 0, 4), Map.of("flag", true)));
        return new WorldSnapshot(
            tick,
            entities,
            new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 12f, 1f)
        );
    }
}
