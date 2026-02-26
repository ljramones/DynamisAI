package org.dynamisai.perception;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.DefaultWorldStateStore;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PerceptionSystemTest {

    private DefaultWorldStateStore worldStore;
    private DefaultPerceptionSystem perception;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        worldStore = new DefaultWorldStateStore();
        perception = new DefaultPerceptionSystem();
        npc = EntityId.of(1L);

        worldStore.enqueueChange(new WorldChange.EntityStateChange(npc,
            new EntityState(npc, new Location(0, 0, 0), Map.of())));
        worldStore.commitTick();
    }

    @Test
    void emptyWorldProducesEmptySnapshot() {
        PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertEquals(0, snap.percepts().size());
        assertEquals(ThreatLevel.NONE, snap.aggregateThreat());
        assertTrue(snap.mostSalientPercept().isEmpty());
    }

    @Test
    void nearbyEntityProducesPercept() {
        EntityId enemy = EntityId.of(2L);
        worldStore.enqueueChange(new WorldChange.EntityStateChange(enemy,
            new EntityState(enemy, new Location(10, 0, 0), Map.of())));
        worldStore.enqueueChange(new WorldChange.FactChange("threatLevel", ThreatLevel.HIGH));
        worldStore.commitTick();

        PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertEquals(1, snap.percepts().size());
        assertEquals(enemy, snap.percepts().get(0).source());
    }

    @Test
    void mostSalientPerceptIsPresent() {
        EntityId e = EntityId.of(3L);
        worldStore.enqueueChange(new WorldChange.EntityStateChange(e,
            new EntityState(e, new Location(5, 0, 0), Map.of())));
        worldStore.commitTick();

        PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertFalse(snap.mostSalientPercept().isEmpty());
    }

    @Test
    void fearModulatesThreateningSalienceUpward() {
        EntityId threat = EntityId.of(4L);
        worldStore.enqueueChange(new WorldChange.EntityStateChange(threat,
            new EntityState(threat, new Location(5, 0, 0), Map.of())));
        worldStore.enqueueChange(new WorldChange.FactChange("threatLevel", ThreatLevel.HIGH));
        worldStore.commitTick();

        PerceptionSnapshot calmSnap = perception.tick(npc, AffectVector.neutral(), worldStore);
        PerceptionSnapshot fearSnap = perception.tick(npc, AffectVector.fearful(), worldStore);

        float calmSalience = calmSnap.percepts().isEmpty() ? 0f :
            calmSnap.percepts().get(0).salienceScore();
        float fearSalience = fearSnap.percepts().isEmpty() ? 0f :
            fearSnap.percepts().get(0).salienceScore();

        assertTrue(fearSalience >= calmSalience,
            "Fear should raise or maintain threat salience: calm=" +
                calmSalience + " fear=" + fearSalience);
    }

    @Test
    void snapshotTickMatchesWorldTick() {
        worldStore.commitTick();
        worldStore.commitTick();
        PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertEquals(worldStore.getCurrentTick(), snap.tick());
    }

    @Test
    void getLastSnapshotReturnsMostRecentTick() {
        perception.tick(npc, AffectVector.neutral(), worldStore);
        worldStore.commitTick();
        PerceptionSnapshot snap2 = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertEquals(snap2.tick(), perception.getLastSnapshot(npc).tick());
    }

    @Test
    void getLastSnapshotForUnknownNpcReturnsEmpty() {
        EntityId unknown = EntityId.of(999L);
        PerceptionSnapshot snap = perception.getLastSnapshot(unknown);
        assertTrue(snap.percepts().isEmpty());
        assertEquals(-1L, snap.tick());
    }

    @Test
    void perceptSalienceIsInRange() {
        EntityId e = EntityId.of(5L);
        worldStore.enqueueChange(new WorldChange.EntityStateChange(e,
            new EntityState(e, new Location(15, 0, 0), Map.of())));
        worldStore.commitTick();

        PerceptionSnapshot snap = perception.tick(npc, AffectVector.angry(), worldStore);
        for (Percept p : snap.percepts()) {
            assertTrue(p.salienceScore() >= 0f && p.salienceScore() <= 1f,
                "Salience out of range: " + p.salienceScore());
            assertTrue(p.rawIntensity() >= 0f && p.rawIntensity() <= 1f,
                "RawIntensity out of range: " + p.rawIntensity());
        }
    }

    @Test
    void perceptSnapshotIsImmutable() {
        EntityId e = EntityId.of(6L);
        worldStore.enqueueChange(new WorldChange.EntityStateChange(e,
            new EntityState(e, new Location(5, 0, 0), Map.of())));
        worldStore.commitTick();

        PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
        assertThrows(UnsupportedOperationException.class,
            () -> snap.percepts().add(null));
    }

    @Test
    void emptySnapshotFactory() {
        PerceptionSnapshot empty = PerceptionSnapshot.empty(npc, 0L, new Location(0, 0, 0));
        assertTrue(empty.percepts().isEmpty());
        assertTrue(empty.mostSalientPercept().isEmpty());
        assertEquals(ThreatLevel.NONE, empty.aggregateThreat());
    }

    @Test
    void perceptRejectsRawIntensityOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            new Percept(npc, StimulusType.VISUAL, new Location(0, 0, 0),
                1.5f, 0.5f, ThreatLevel.NONE, false));
    }

    @Test
    void perceptRejectsSalienceOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            new Percept(npc, StimulusType.VISUAL, new Location(0, 0, 0),
                0.5f, -0.1f, ThreatLevel.NONE, false));
    }
}
