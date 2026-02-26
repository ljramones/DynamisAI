package org.dynamisai.lod;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.LodTier;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldSnapshot;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Lightweight importance scoring with no external system calls.
 */
public final class DefaultImportanceEvaluator implements ImportanceEvaluator {

    private static final float DEFAULT_MAX_DISTANCE = 200f;

    private final EntityId observer;
    private final float maxDistance;
    private final Supplier<Set<EntityId>> dialogueInFlightSupplier;

    public DefaultImportanceEvaluator(EntityId observer) {
        this(observer, DEFAULT_MAX_DISTANCE, Set::of);
    }

    public DefaultImportanceEvaluator(EntityId observer,
                                      float maxDistance,
                                      Supplier<Set<EntityId>> dialogueInFlightSupplier) {
        this.observer = observer;
        this.maxDistance = maxDistance;
        this.dialogueInFlightSupplier = dialogueInFlightSupplier;
    }

    @Override
    public ImportanceScore evaluate(EntityId entityId, WorldSnapshot snapshot) {
        if (entityId.equals(observer)) {
            return new ImportanceScore(entityId, 1.0f, LodTier.TIER_0, snapshot.tick());
        }

        EntityState observerState = snapshot.entities().get(observer).getOrNull();
        EntityState entityState = snapshot.entities().get(entityId).getOrNull();

        float score = 0f;
        if (observerState != null && entityState != null) {
            float distance = distance(observerState.position(), entityState.position());
            score += clamp01(1.0f - (distance / maxDistance));
        }

        score += threatBoost(entityState);

        if (hasDialogueInFlight(entityId, entityState)) {
            score += 0.3f;
        }

        score = clamp01(score);
        return new ImportanceScore(entityId, score, tierFromScore(score), snapshot.tick());
    }

    private boolean hasDialogueInFlight(EntityId entityId, EntityState state) {
        if (dialogueInFlightSupplier.get().contains(entityId)) {
            return true;
        }
        if (state == null || state.properties() == null) {
            return false;
        }
        Object prop = state.properties().get("dialogueInFlight");
        return prop instanceof Boolean b && b;
    }

    private static float threatBoost(EntityState state) {
        if (state == null || state.properties() == null) {
            return 0f;
        }
        Object value = state.properties().get("threatLevel");
        if (!(value instanceof ThreatLevel t)) {
            return 0f;
        }
        return switch (t) {
            case CRITICAL -> 0.4f;
            case HIGH -> 0.2f;
            case LOW -> -0.1f;
            case NONE, MEDIUM -> 0f;
        };
    }

    private static LodTier tierFromScore(float score) {
        if (score >= 0.75f) return LodTier.TIER_0;
        if (score >= 0.5f) return LodTier.TIER_1;
        if (score >= 0.25f) return LodTier.TIER_2;
        return LodTier.TIER_3;
    }

    private static float distance(Location a, Location b) {
        float dx = (float) (a.x() - b.x());
        float dy = (float) (a.y() - b.y());
        float dz = (float) (a.z() - b.z());
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
