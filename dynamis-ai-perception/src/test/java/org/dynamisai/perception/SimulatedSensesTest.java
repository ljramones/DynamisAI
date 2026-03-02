package org.dynamisai.perception;

import io.vavr.collection.HashMap;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.Location;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatedSensesTest {

    private SensorProfileRegistry registry;
    private SoundEventQueue queue;
    private SimulatedSenses senses;
    private EntityId observer;

    @BeforeEach
    void setUp() {
        registry = new SensorProfileRegistry();
        queue = new SoundEventQueue();
        senses = new SimulatedSenses(registry, queue);
        observer = EntityId.of(1L);
    }

    @Test
    void inVisionAndFovProducesVisualPercept() {
        WorldSnapshot snapshot = snapshotOf(
            observer, state(0f, 0f),
            EntityId.of(2L), state(0f, -10f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().anyMatch(p -> p.stimulusType() == StimulusType.VISUAL));
    }

    @Test
    void inVisionButOutsideFovIsNotVisible() {
        registry.register(observer, new SensorProfile(30f, 60f, 20f, 0.5f, 0f));
        WorldSnapshot snapshot = snapshotOf(
            observer, state(0f, 0f),
            EntityId.of(2L), state(10f, 0f)); // east, outside narrow north-facing cone

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().noneMatch(p -> p.source().equals(EntityId.of(2L))));
    }

    @Test
    void beyondVisionRadiusNotVisible() {
        WorldSnapshot snapshot = snapshotOf(
            observer, state(0f, 0f),
            EntityId.of(2L), state(0f, -100f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().isEmpty());
    }

    @Test
    void observerItselfExcluded() {
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));
        PerceptionSnapshot result = senses.sense(observer, snapshot);
        assertTrue(result.percepts().stream().noneMatch(p -> p.source().equals(observer)));
    }

    @Test
    void audibleSoundAboveAcuityIncluded() {
        queue.post(new SoundEvent(EntityId.of(2L), new Location(0f, 0f, -2f),
            1f, StimulusType.AUDITORY, 1L));
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().anyMatch(p -> p.stimulusType() == StimulusType.AUDITORY));
    }

    @Test
    void audibleSoundBelowAcuityExcluded() {
        registry.register(observer, new SensorProfile(30f, 120f, 20f, 0.9f, 0f));
        queue.post(new SoundEvent(EntityId.of(2L), new Location(0f, 0f, -2f),
            0.2f, StimulusType.AUDITORY, 1L));
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().noneMatch(p -> p.stimulusType() == StimulusType.AUDITORY));
    }

    @Test
    void soundBeyondHearingRadiusExcluded() {
        queue.post(new SoundEvent(EntityId.of(2L), new Location(0f, 0f, -100f),
            1f, StimulusType.AUDITORY, 1L));
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().noneMatch(p -> p.stimulusType() == StimulusType.AUDITORY));
    }

    @Test
    void blindEntityHasNoVisualPercepts() {
        registry.register(observer, SensorProfile.blind());
        WorldSnapshot snapshot = snapshotOf(
            observer, state(0f, 0f),
            EntityId.of(2L), state(0f, -1f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().noneMatch(p -> p.stimulusType() == StimulusType.VISUAL));
    }

    @Test
    void deafEntityHasNoAuditoryPercepts() {
        registry.register(observer, SensorProfile.deaf());
        queue.post(new SoundEvent(EntityId.of(2L), new Location(0f, 0f, -1f),
            1f, StimulusType.AUDITORY, 1L));
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));

        PerceptionSnapshot result = senses.sense(observer, snapshot);

        assertTrue(result.percepts().stream().noneMatch(p -> p.stimulusType() == StimulusType.AUDITORY));
    }

    @Test
    void emptySnapshotProducesEmptyPercepts() {
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));
        PerceptionSnapshot result = senses.sense(EntityId.of(999L), snapshot);
        assertTrue(result.percepts().isEmpty());
    }

    @Test
    void returnedOwnerMatchesObserver() {
        WorldSnapshot snapshot = snapshotOf(observer, state(0f, 0f));
        PerceptionSnapshot result = senses.sense(observer, snapshot);
        assertEquals(observer, result.owner());
    }

    @Test
    void perceptionSnapshotFactoryBuildsExpectedOwner() {
        PerceptionSnapshot snapshot = PerceptionSnapshot.of(observer, List.of(), 7L);
        assertEquals(observer, snapshot.owner());
        assertEquals(7L, snapshot.tick());
    }

    private static WorldSnapshot snapshotOf(Object... entries) {
        HashMap<EntityId, EntityState> map = HashMap.empty();
        for (int i = 0; i < entries.length; i += 2) {
            EntityId id = (EntityId) entries[i];
            EntityState state = (EntityState) entries[i + 1];
            map = map.put(id, new EntityState(id, state.position(), state.properties()));
        }
        return new WorldSnapshot(1L, map, new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 12f, 1f));
    }

    private static EntityState state(float x, float z) {
        return new EntityState(EntityId.NONE, new Location(x, 0f, z), Map.of());
    }
}
