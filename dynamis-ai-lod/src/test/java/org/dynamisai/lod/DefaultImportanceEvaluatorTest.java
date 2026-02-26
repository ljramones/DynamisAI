package org.dynamisai.lod;

import io.vavr.collection.HashMap;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.LodTier;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultImportanceEvaluatorTest {

    @Test
    void observerAlwaysTier0() {
        EntityId observer = EntityId.of(1L);
        WorldSnapshot snapshot = snapshot(
            observer, state(0, 0, ThreatLevel.NONE, false),
            EntityId.of(2L), state(100, 0, ThreatLevel.NONE, false));

        DefaultImportanceEvaluator evaluator = new DefaultImportanceEvaluator(observer);
        ImportanceScore score = evaluator.evaluate(observer, snapshot);

        assertEquals(LodTier.TIER_0, score.assignedTier());
        assertEquals(1.0f, score.score(), 0.0001f);
    }

    @Test
    void distanceZeroReturnsTier0() {
        EntityId observer = EntityId.of(1L);
        EntityId other = EntityId.of(2L);
        WorldSnapshot snapshot = snapshot(
            observer, state(0, 0, ThreatLevel.NONE, false),
            other, state(0, 0, ThreatLevel.NONE, true));

        DefaultImportanceEvaluator evaluator =
            new DefaultImportanceEvaluator(observer, 200f, () -> Set.of(other));
        ImportanceScore score = evaluator.evaluate(other, snapshot);

        assertEquals(LodTier.TIER_0, score.assignedTier());
    }

    @Test
    void farDistanceReturnsTier3() {
        EntityId observer = EntityId.of(1L);
        EntityId far = EntityId.of(2L);
        WorldSnapshot snapshot = snapshot(
            observer, state(0, 0, ThreatLevel.NONE, false),
            far, state(1000, 0, ThreatLevel.NONE, false));

        DefaultImportanceEvaluator evaluator = new DefaultImportanceEvaluator(observer);
        ImportanceScore score = evaluator.evaluate(far, snapshot);

        assertEquals(LodTier.TIER_3, score.assignedTier());
    }

    @Test
    void criticalThreatScoresHigherThanNormalAtSameDistance() {
        EntityId observer = EntityId.of(1L);
        EntityId normal = EntityId.of(2L);
        EntityId critical = EntityId.of(3L);
        WorldSnapshot snapshot = snapshot(
            observer, state(0, 0, ThreatLevel.NONE, false),
            normal, state(50, 0, ThreatLevel.NONE, false),
            critical, state(50, 0, ThreatLevel.CRITICAL, false));

        DefaultImportanceEvaluator evaluator = new DefaultImportanceEvaluator(observer);
        ImportanceScore normalScore = evaluator.evaluate(normal, snapshot);
        ImportanceScore criticalScore = evaluator.evaluate(critical, snapshot);

        assertTrue(criticalScore.score() > normalScore.score());
    }

    @Test
    void scoreIsClampedToZeroAndOne() {
        EntityId observer = EntityId.of(1L);
        EntityId entity = EntityId.of(2L);
        WorldSnapshot snapshot = snapshot(
            observer, state(0, 0, ThreatLevel.NONE, false),
            entity, state(0, 0, ThreatLevel.CRITICAL, true));

        DefaultImportanceEvaluator evaluator =
            new DefaultImportanceEvaluator(observer, 200f, () -> Set.of(entity));
        ImportanceScore score = evaluator.evaluate(entity, snapshot);

        assertTrue(score.score() >= 0f);
        assertTrue(score.score() <= 1f);
    }

    private static EntityState state(float x, float z, ThreatLevel threat, boolean dialogue) {
        return new EntityState(
            EntityId.of(-1L),
            new Location(x, 0, z),
            Map.of("threatLevel", threat, "dialogueInFlight", dialogue));
    }

    private static WorldSnapshot snapshot(Object... pairs) {
        HashMap<EntityId, EntityState> map = HashMap.empty();
        for (int i = 0; i < pairs.length; i += 2) {
            EntityId id = (EntityId) pairs[i];
            EntityState s = (EntityState) pairs[i + 1];
            map = map.put(id, new EntityState(id, s.position(), s.properties()));
        }
        return new WorldSnapshot(
            1L,
            map,
            new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 12.0f, 1.0f)
        );
    }
}
