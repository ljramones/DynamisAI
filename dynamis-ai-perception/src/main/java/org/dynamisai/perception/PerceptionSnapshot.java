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
    /** Convenience factory used by sense simulation pipelines. */
    public static PerceptionSnapshot of(EntityId owner, List<Percept> percepts, long tick) {
        Optional<Percept> mostSalient = percepts.stream()
            .max((a, b) -> Float.compare(a.salienceScore(), b.salienceScore()));
        ThreatLevel aggregate = percepts.stream()
            .map(Percept::perceivedThreat)
            .max(Enum::compareTo)
            .orElse(ThreatLevel.NONE);
        return new PerceptionSnapshot(
            owner,
            tick,
            List.copyOf(percepts),
            mostSalient,
            aggregate,
            new Location(0f, 0f, 0f),
            percepts.size());
    }

    /** Empty snapshot — no perceptions this frame. */
    public static PerceptionSnapshot empty(EntityId owner, long tick, Location location) {
        return new PerceptionSnapshot(owner, tick, List.of(),
            Optional.empty(), ThreatLevel.NONE, location, 0);
    }
}
