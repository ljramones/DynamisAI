package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of entity standing with all factions.
 */
public final class FactionRegistry {

    // Key: "entityId:factionName"
    private final ConcurrentHashMap<String, FactionStanding> standings =
        new ConcurrentHashMap<>();

    /** Get standing — returns unknown() if no record exists. */
    public FactionStanding get(EntityId entity, FactionId faction) {
        return standings.getOrDefault(key(entity, faction),
            FactionStanding.unknown(faction));
    }

    /** Set standing directly. */
    public void set(EntityId entity, FactionId faction, FactionStanding standing) {
        standings.put(key(entity, faction), standing);
    }

    /**
     * Adjust standing by delta. Clamps to [-1, 1].
     * Creates unknown() baseline if no record exists.
     */
    public void adjust(EntityId entity, FactionId faction, float delta) {
        standings.compute(key(entity, faction), (k, existing) -> {
            FactionStanding base = existing != null
                ? existing : FactionStanding.unknown(faction);
            return base.withStanding(base.standing() + delta);
        });
    }

    /** Add contribution points and recalculate standing. */
    public void addPoints(EntityId entity, FactionId faction, long points) {
        standings.compute(key(entity, faction), (k, existing) -> {
            FactionStanding base = existing != null
                ? existing : FactionStanding.unknown(faction);
            FactionStanding updated = base.addPoints(points);
            float newStanding = Math.max(-1f, Math.min(1f,
                updated.contributionPoints() / 1000f));
            return updated.withStanding(newStanding);
        });
    }

    /** All factions this entity has any standing with. */
    public List<FactionStanding> standingsFor(EntityId entity) {
        String prefix = entity.value() + ":";
        List<FactionStanding> result = new ArrayList<>();
        for (Map.Entry<String, FactionStanding> entry : standings.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /** Remove all faction standings for an entity — call on despawn. */
    public void removeEntity(EntityId entity) {
        String prefix = entity.value() + ":";
        standings.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** Total (entity, faction) pairs tracked. */
    public int size() { return standings.size(); }

    private static String key(EntityId entity, FactionId faction) {
        return entity.value() + ":" + faction.name();
    }
}
