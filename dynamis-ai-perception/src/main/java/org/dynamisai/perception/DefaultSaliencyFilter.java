package org.dynamisai.perception;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DefaultSaliencyFilter implements SaliencyFilter {

    private static final float MIN_SALIENCE = 0.1f;
    private static final float MAX_RANGE = 80f;
    private static final int MAX_PERCEPTS = 20;

    @Override
    public PerceptionSnapshot filter(EntityId owner, WorldFacts facts,
                                     AffectVector affect, long tick) {
        Location origin = facts.agentPosition();
        List<EntityId> nearby = facts.nearbyActors();
        ThreatLevel globalThreat = facts.currentThreat();

        List<Percept> percepts = new ArrayList<>();

        for (EntityId source : nearby) {
            // Placeholder values until physical sensors and spatial links are wired.
            float simulatedDistance = 10f;
            float rawIntensity = 0.7f;

            float distanceFalloff = Math.max(0f, 1f - (simulatedDistance / MAX_RANGE));
            float baseSalience = rawIntensity * distanceFalloff;

            ThreatLevel threat = globalThreat;
            boolean isNovel = false;

            float salience = baseSalience;

            if (threat != ThreatLevel.NONE) {
                salience *= (1f + affect.arousal() * 0.5f);
            }

            if (isNovel) {
                salience *= 1.2f;
            }

            if (affect.valence() < -0.3f && threat != ThreatLevel.NONE) {
                salience += 0.3f;
            }

            salience = Math.min(1f, salience);

            if (salience < MIN_SALIENCE) continue;

            percepts.add(new Percept(
                source, StimulusType.VISUAL, origin,
                rawIntensity, salience, threat, isNovel
            ));
        }

        percepts.sort(Comparator.comparingDouble(Percept::salienceScore).reversed());
        if (percepts.size() > MAX_PERCEPTS) {
            percepts = new ArrayList<>(percepts.subList(0, MAX_PERCEPTS));
        }

        ThreatLevel aggregate = percepts.stream()
            .map(Percept::perceivedThreat)
            .max(Comparator.comparingInt(Enum::ordinal))
            .orElse(ThreatLevel.NONE);

        Optional<Percept> mostSalient = percepts.isEmpty()
            ? Optional.empty()
            : Optional.of(percepts.get(0));

        return new PerceptionSnapshot(
            owner, tick,
            Collections.unmodifiableList(new ArrayList<>(percepts)),
            mostSalient,
            aggregate,
            origin,
            nearby.size()
        );
    }
}
