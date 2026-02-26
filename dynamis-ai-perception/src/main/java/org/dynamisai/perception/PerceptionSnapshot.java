package org.dynamisai.perception;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;

import java.util.List;
import java.util.Optional;

/**
 * The NPC's complete sensory frame — produced once per tick by PerceptionSystem.
 * The cognitive stack reads only this, never WorldStateStore directly.
 */
public record PerceptionSnapshot(
    EntityId owner,
    long tick,
    List<Percept> percepts,
    Optional<Percept> mostSalientPercept,
    ThreatLevel aggregateThreat,
    Location ownerLocation,
    int totalEntitiesInRange
) {
    /** Empty snapshot — no perceptions this frame. */
    public static PerceptionSnapshot empty(EntityId owner, long tick, Location location) {
        return new PerceptionSnapshot(owner, tick, List.of(),
            Optional.empty(), ThreatLevel.NONE, location, 0);
    }
}
