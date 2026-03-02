package org.dynamisai.lod;

import io.vavr.collection.HashMap;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.LodTier;
import org.dynamisai.core.Location;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AILODPolicyTest {

    @Test
    void evaluateReturnsTierForEveryEntity() {
        AILODPolicy policy = new AILODPolicy((id, snap) ->
            new ImportanceScore(id, 0.8f, LodTier.TIER_0, snap.tick()));
        WorldSnapshot snapshot = snapshot(10L, EntityId.of(1L), EntityId.of(2L));

        Map<EntityId, LodTier> tiers = policy.evaluate(snapshot);
        assertEquals(2, tiers.size());
    }

    @Test
    void currentTierUnknownDefaultsToTier3() {
        AILODPolicy policy = new AILODPolicy((id, snap) ->
            new ImportanceScore(id, 0.8f, LodTier.TIER_0, snap.tick()));
        assertEquals(LodTier.TIER_3, policy.currentTier(EntityId.of(999L)));
    }

    @Test
    void shouldRunAiFalseForTier3OnNonDivisibleTick() {
        AILODPolicy policy = new AILODPolicy((id, snap) ->
            new ImportanceScore(id, 0.1f, LodTier.TIER_3, snap.tick()));
        EntityId id = EntityId.of(1L);
        WorldSnapshot snapshot = snapshot(1L, id);

        assertFalse(policy.shouldRunAi(id, 3L, snapshot));
    }

    @Test
    void shouldRunAiTrueForTier0Always() {
        AILODPolicy policy = new AILODPolicy((id, snap) ->
            new ImportanceScore(id, 0.9f, LodTier.TIER_0, snap.tick()));
        EntityId id = EntityId.of(1L);
        WorldSnapshot snapshot = snapshot(1L, id);

        assertTrue(policy.shouldRunAi(id, 3L, snapshot));
    }

    @Test
    void evaluateUsesCacheBetweenIntervals() {
        AtomicInteger calls = new AtomicInteger();
        AILODPolicy policy = new AILODPolicy((id, snap) -> {
            calls.incrementAndGet();
            return new ImportanceScore(id, 0.6f, LodTier.TIER_1, snap.tick());
        });

        EntityId id = EntityId.of(1L);
        policy.evaluate(snapshot(10L, id));
        policy.evaluate(snapshot(15L, id));
        assertEquals(1, calls.get());

        policy.evaluate(snapshot(20L, id));
        assertEquals(2, calls.get());
    }

    @Test
    void currentTierReflectsCachedEvaluation() {
        EntityId id = EntityId.of(5L);
        AILODPolicy policy = new AILODPolicy((e, snap) ->
            new ImportanceScore(e, 0.6f, LodTier.TIER_1, snap.tick()));
        policy.evaluate(snapshot(10L, id));
        assertEquals(LodTier.TIER_1, policy.currentTier(id));
    }

    @Test
    void shouldRunAiTrueForTier3OnTick16() {
        EntityId id = EntityId.of(1L);
        AILODPolicy policy = new AILODPolicy((e, snap) ->
            new ImportanceScore(e, 0.1f, LodTier.TIER_3, snap.tick()));
        WorldSnapshot snapshot = snapshot(1L, id);
        assertTrue(policy.shouldRunAi(id, 16L, snapshot));
    }

    @Test
    void evaluateCallsEvaluatorPerEntityOnRefresh() {
        AtomicInteger calls = new AtomicInteger();
        AILODPolicy policy = new AILODPolicy((id, snap) -> {
            calls.incrementAndGet();
            return new ImportanceScore(id, 0.8f, LodTier.TIER_0, snap.tick());
        });
        policy.evaluate(snapshot(10L, EntityId.of(1L), EntityId.of(2L), EntityId.of(3L)));
        assertEquals(3, calls.get());
    }

    private static WorldSnapshot snapshot(long tick, EntityId... ids) {
        HashMap<EntityId, EntityState> map = HashMap.empty();
        for (EntityId id : ids) {
            map = map.put(id, new EntityState(id, new Location(id.id(), 0, id.id()), Map.of()));
        }
        return new WorldSnapshot(
            tick,
            map,
            new GlobalFacts(Map.of()),
            new EnvironmentState("clear", 12.0f, 1.0f)
        );
    }
}
