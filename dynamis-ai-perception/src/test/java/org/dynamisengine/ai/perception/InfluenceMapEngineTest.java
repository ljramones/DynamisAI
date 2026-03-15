package org.dynamisengine.ai.perception;

import io.vavr.collection.HashMap;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.EnvironmentState;
import org.dynamisengine.ai.core.GlobalFacts;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InfluenceMapEngineTest {

    @Test
    void freshEngineStartsWithZeroLayers() {
        InfluenceMapEngine engine = new InfluenceMapEngine(12, 12, 1f, new Location(0, 0, 0));

        for (InfluenceLayer layer : InfluenceLayer.values()) {
            for (float v : engine.grid(layer).rawSnapshot()) {
                assertEquals(0f, v, 1e-6f);
            }
        }
    }

    @Test
    void updateWithCriticalThreatRaisesThreatLayer() {
        InfluenceMapEngine engine = new InfluenceMapEngine(20, 20, 1f, new Location(0, 0, 0));
        WorldSnapshot snapshot = snapshotWithEntity(
            EntityId.of(1L),
            new Location(10f, 0f, 10f),
            Map.of("threatLevel", ThreatLevel.CRITICAL));

        engine.update(snapshot, List.of());

        assertTrue(engine.grid(InfluenceLayer.THREAT).sampleAt(new Location(10.5f, 0f, 10.5f)) > 0.7f);
    }

    @Test
    void updateWithSoundEventRaisesSoundLayer() {
        InfluenceMapEngine engine = new InfluenceMapEngine(20, 20, 1f, new Location(0, 0, 0));
        WorldSnapshot snapshot = emptySnapshot();

        engine.update(snapshot, List.of(new SoundEvent(
            EntityId.of(2L), new Location(8f, 0f, 8f), 1.0f, StimulusType.AUDITORY, 1L)));

        assertTrue(engine.grid(InfluenceLayer.SOUND).sampleAt(new Location(8.5f, 0f, 8.5f)) > 0.5f);
    }

    @Test
    void threatDecaysWhenNoEntitiesPresent() {
        InfluenceMapEngine engine = new InfluenceMapEngine(20, 20, 1f, new Location(0, 0, 0));
        WorldSnapshot withThreat = snapshotWithEntity(
            EntityId.of(1L),
            new Location(10f, 0f, 10f),
            Map.of("threatLevel", ThreatLevel.CRITICAL));

        engine.update(withThreat, List.of());
        float first = engine.grid(InfluenceLayer.THREAT).sampleAt(new Location(10.5f, 0f, 10.5f));

        engine.update(emptySnapshot(), List.of());
        float second = engine.grid(InfluenceLayer.THREAT).sampleAt(new Location(10.5f, 0f, 10.5f));

        assertTrue(second < first);
    }

    @Test
    void snapshotIsImmutableCopy() {
        InfluenceMapEngine engine = new InfluenceMapEngine(8, 8, 1f, new Location(0, 0, 0));
        engine.grid(InfluenceLayer.THREAT).set(2, 2, 0.8f);

        InfluenceMapSnapshot snap = engine.snapshot(1L);
        engine.grid(InfluenceLayer.THREAT).set(2, 2, 0f);

        assertEquals(0.8f,
            snap.sample(InfluenceLayer.THREAT, new Location(2.5f, 0f, 2.5f)), 1e-6f);
    }

    @Test
    void addCoverPersistsAcrossUpdateCalls() {
        InfluenceMapEngine engine = new InfluenceMapEngine(10, 10, 1f, new Location(0, 0, 0));
        engine.addCoverAt(new Location(3f, 0f, 3f), 0.9f);

        float before = engine.grid(InfluenceLayer.COVER).sampleAt(new Location(3.5f, 0f, 3.5f));
        engine.update(emptySnapshot(), List.of());
        float after = engine.grid(InfluenceLayer.COVER).sampleAt(new Location(3.5f, 0f, 3.5f));

        assertEquals(before, after, 1e-6f);
    }

    @Test
    void territorialLayerSupportsPositiveAndNegativeContributions() {
        InfluenceMapEngine engine = new InfluenceMapEngine(20, 20, 1f, new Location(0, 0, 0));
        HashMap<EntityId, EntityState> entities = HashMap.empty();
        entities = entities.put(EntityId.of(1L),
            new EntityState(EntityId.of(1L), new Location(4f, 0f, 4f), Map.of("faction", "friendly")));
        entities = entities.put(EntityId.of(2L),
            new EntityState(EntityId.of(2L), new Location(14f, 0f, 14f), Map.of("faction", "hostile")));

        WorldSnapshot snapshot = new WorldSnapshot(1L, entities,
            new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));

        engine.update(snapshot, List.of());

        float friendly = engine.grid(InfluenceLayer.TERRITORIAL).sampleAt(new Location(4.5f, 0f, 4.5f));
        float hostile = engine.grid(InfluenceLayer.TERRITORIAL).sampleAt(new Location(14.5f, 0f, 14.5f));

        assertTrue(friendly > 0f);
        assertTrue(hostile < 0f);
    }

    @Test
    void gridReturnsLiveMutableGrid() {
        InfluenceMapEngine engine = new InfluenceMapEngine(10, 10, 1f, new Location(0, 0, 0));
        InfluenceGrid movement = engine.grid(InfluenceLayer.MOVEMENT);

        movement.set(1, 1, 0.77f);

        assertEquals(0.77f, engine.grid(InfluenceLayer.MOVEMENT).get(1, 1), 1e-6f);
    }

    private static WorldSnapshot snapshotWithEntity(EntityId id, Location pos, Map<String, Object> props) {
        HashMap<EntityId, EntityState> entities = HashMap.<EntityId, EntityState>empty()
            .put(id, new EntityState(id, pos, props));
        return new WorldSnapshot(1L, entities,
            new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }

    private static WorldSnapshot emptySnapshot() {
        return new WorldSnapshot(1L, HashMap.empty(),
            new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }
}
