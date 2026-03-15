package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Computes and applies event-driven social reputation deltas to SocialGraph. */
public final class ReputationEngine {

    private static final float HEARD_MULTIPLIER = 0.5f;

    private static final record DeltaScale(float trust, float affinity) {}

    private final Map<ReputationEventType, DeltaScale> scales;

    public ReputationEngine() {
        this.scales = defaultScales();
    }

    public ReputationEngine(Map<ReputationEventType, Float> magnitudeScales) {
        EnumMap<ReputationEventType, DeltaScale> map = new EnumMap<>(ReputationEventType.class);
        Map<ReputationEventType, DeltaScale> defaults = defaultScales();
        for (ReputationEventType type : ReputationEventType.values()) {
            float multiplier = magnitudeScales.getOrDefault(type, 1.0f);
            DeltaScale d = defaults.get(type);
            map.put(type, new DeltaScale(d.trust * multiplier, d.affinity * multiplier));
        }
        this.scales = Map.copyOf(map);
    }

    public ReputationDelta compute(ReputationEvent event) {
        DeltaScale scale = scales.get(event.type());
        float trust = scale.trust * event.magnitude();
        float affinity = scale.affinity * event.magnitude();
        if (!event.isWitnessed()) {
            trust *= HEARD_MULTIPLIER;
            affinity *= HEARD_MULTIPLIER;
        }

        Set<RelationshipTag> add = switch (event.type()) {
            case BETRAYED, DECLARED_HOSTILE, HARMED -> Set.of(RelationshipTag.ENEMY);
            case THREATENED, INSULTED -> Set.of(RelationshipTag.RIVAL);
            case SAVED, ALLIED -> Set.of(RelationshipTag.ALLY);
            default -> Set.of();
        };
        Set<RelationshipTag> remove = switch (event.type()) {
            case BETRAYED, DECLARED_HOSTILE -> Set.of(RelationshipTag.ALLY);
            default -> Set.of();
        };
        return new ReputationDelta(trust, affinity, add, remove);
    }

    public ReputationDelta apply(ReputationEvent event, SocialGraph graph) {
        ReputationDelta base = compute(event);

        EntityId actor = event.actor();
        EntityId target = event.target();

        Relationship current = graph.get(actor, target);
        float nextTrust = clamp(current.trust() + base.trustDelta());
        float nextAffinity = clamp(current.affinity() + base.affinityDelta());

        graph.update(actor, target, r -> {
            Relationship updated = r.withTrust(nextTrust)
                .withAffinity(nextAffinity)
                .recordInteraction(java.time.Instant.now());
            for (RelationshipTag remove : base.tagsToRemove()) {
                updated = updated.withoutTag(remove);
            }
            for (RelationshipTag add : base.tagsToAdd()) {
                updated = updated.withTag(add);
            }
            // Threshold tags from resulting relationship state.
            if (event.type() == ReputationEventType.HELPED && updated.trust() > 0.7f) {
                updated = updated.withTag(RelationshipTag.ALLY);
            }
            if (event.type() == ReputationEventType.HARMED && updated.trust() < -0.5f) {
                updated = updated.withTag(RelationshipTag.ENEMY);
            }
            if (event.type() == ReputationEventType.INSULTED && updated.affinity() < -0.3f) {
                updated = updated.withTag(RelationshipTag.RIVAL);
            }
            return updated;
        });

        return base;
    }

    public List<ReputationDelta> applyAll(List<ReputationEvent> events, SocialGraph graph) {
        List<ReputationEvent> ordered = new ArrayList<>(events);
        ordered.sort(Comparator.comparingLong(ReputationEvent::tick));
        List<ReputationDelta> deltas = new ArrayList<>(ordered.size());
        for (ReputationEvent event : ordered) {
            deltas.add(apply(event, graph));
        }
        return List.copyOf(deltas);
    }

    private static Map<ReputationEventType, DeltaScale> defaultScales() {
        EnumMap<ReputationEventType, DeltaScale> map = new EnumMap<>(ReputationEventType.class);
        map.put(ReputationEventType.HELPED, new DeltaScale(0.1f, 0.1f));
        map.put(ReputationEventType.HARMED, new DeltaScale(-0.2f, -0.15f));
        map.put(ReputationEventType.BETRAYED, new DeltaScale(-0.4f, -0.3f));
        map.put(ReputationEventType.SAVED, new DeltaScale(0.3f, 0.25f));
        map.put(ReputationEventType.THREATENED, new DeltaScale(-0.15f, -0.2f));
        map.put(ReputationEventType.GIFTED, new DeltaScale(0.05f, 0.15f));
        map.put(ReputationEventType.INSULTED, new DeltaScale(-0.05f, -0.2f));
        map.put(ReputationEventType.ALLIED, new DeltaScale(0.2f, 0.1f));
        map.put(ReputationEventType.DECLARED_HOSTILE, new DeltaScale(-0.3f, -0.2f));
        return Map.copyOf(map);
    }

    private static float clamp(float value) {
        return Math.max(-1f, Math.min(1f, value));
    }
}
