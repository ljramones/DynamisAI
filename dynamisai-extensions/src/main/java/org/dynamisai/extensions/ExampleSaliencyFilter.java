package org.dynamisai.extensions;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.perception.Percept;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.perception.SaliencyFilter;
import org.dynamisai.perception.StimulusType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Minimal SaliencyFilter SPI example.
 *
 * A real filter would score and rank percepts by threat/novelty/affect; this
 * stub emits one neutral SOCIAL percept per nearby actor.
 */
public final class ExampleSaliencyFilter implements SaliencyFilter {

    @Override
    public PerceptionSnapshot filter(EntityId owner, WorldFacts facts, AffectVector affect, long tick) {
        Location ownerLoc = facts.agentPosition() == null ? new Location(0, 0, 0) : facts.agentPosition();
        ThreatLevel threat = facts.currentThreat() == null ? ThreatLevel.NONE : facts.currentThreat();

        List<Percept> percepts = new ArrayList<>();
        for (EntityId source : facts.nearbyActors()) {
            percepts.add(new Percept(
                source,
                StimulusType.SOCIAL,
                ownerLoc,
                0.5f,
                0.5f,
                threat,
                false));
        }

        Optional<Percept> mostSalient = percepts.isEmpty()
            ? Optional.empty()
            : Optional.of(percepts.get(0));

        return new PerceptionSnapshot(
            owner,
            tick,
            List.copyOf(percepts),
            mostSalient,
            threat,
            ownerLoc,
            percepts.size());
    }
}
