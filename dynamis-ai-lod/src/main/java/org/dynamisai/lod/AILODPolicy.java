package org.dynamisai.lod;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.LodTier;
import org.dynamisai.core.WorldSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LOD policy coordinator with interval-based cached tier assignments.
 */
public final class AILODPolicy implements org.dynamisai.core.AILODPolicy {

    public static final int LOD_EVAL_INTERVAL_TICKS = 10;

    private final ImportanceEvaluator evaluator;
    private final Map<EntityId, LodTier> cachedTiers = new ConcurrentHashMap<>();
    private volatile long lastEvaluatedTick = Long.MIN_VALUE;

    public AILODPolicy(ImportanceEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Map<EntityId, LodTier> evaluate(WorldSnapshot snapshot) {
        if (needsReevaluation(snapshot.tick())) {
            Map<EntityId, LodTier> updated = new java.util.HashMap<>();
            snapshot.entities().forEach((id, state) -> {
                ImportanceScore score = evaluator.evaluate(id, snapshot);
                updated.put(id, score.assignedTier());
            });
            cachedTiers.clear();
            cachedTiers.putAll(updated);
            lastEvaluatedTick = snapshot.tick();
        }
        return Map.copyOf(cachedTiers);
    }

    @Override
    public boolean shouldRunAi(EntityId entityId, long currentTick, WorldSnapshot snapshot) {
        Map<EntityId, LodTier> tiers = evaluate(snapshot);
        LodTier tier = tiers.getOrDefault(entityId, LodTier.TIER_3);
        return TickScaler.shouldTick(tier, currentTick);
    }

    public LodTier currentTier(EntityId entityId) {
        return cachedTiers.getOrDefault(entityId, LodTier.TIER_3);
    }

    private boolean needsReevaluation(long tick) {
        return lastEvaluatedTick == Long.MIN_VALUE ||
            (tick - lastEvaluatedTick) >= LOD_EVAL_INTERVAL_TICKS;
    }
}
