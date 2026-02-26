package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

/**
 * Directed relationship from one entity to another.
 *
 * Scores are in [-1, 1]:
 *   trust    — reliability; negative = expects betrayal
 *   affinity — emotional warmth; negative = dislike
 *
 * Relationships are stored as directed edges in SocialGraph.
 * A and B each hold their own Relationship toward the other,
 * which may differ (A trusts B more than B trusts A).
 *
 * Immutable — mutations produce new instances via with*() methods.
 */
public record Relationship(
    EntityId from,
    EntityId to,
    float trust,
    float affinity,
    Set<RelationshipTag> tags,
    long interactionCount,
    Instant lastInteraction
) {
    public Relationship {
        trust = Math.max(-1f, Math.min(1f, trust));
        affinity = Math.max(-1f, Math.min(1f, affinity));
        tags = Set.copyOf(tags);
    }

    /** Default neutral relationship — no history. */
    public static Relationship neutral(EntityId from, EntityId to) {
        return new Relationship(from, to, 0f, 0f,
            EnumSet.of(RelationshipTag.NEUTRAL), 0L, Instant.EPOCH);
    }

    public Relationship withTrust(float trust) {
        return new Relationship(from, to, trust, affinity, tags,
            interactionCount, lastInteraction);
    }

    public Relationship withAffinity(float affinity) {
        return new Relationship(from, to, trust, affinity, tags,
            interactionCount, lastInteraction);
    }

    public Relationship withTag(RelationshipTag tag) {
        EnumSet<RelationshipTag> newTags = EnumSet.copyOf(tags);
        newTags.add(tag);
        newTags.remove(RelationshipTag.NEUTRAL);
        return new Relationship(from, to, trust, affinity, newTags,
            interactionCount, lastInteraction);
    }

    public Relationship withoutTag(RelationshipTag tag) {
        EnumSet<RelationshipTag> newTags = EnumSet.copyOf(tags);
        newTags.remove(tag);
        if (newTags.isEmpty()) {
            newTags.add(RelationshipTag.NEUTRAL);
        }
        return new Relationship(from, to, trust, affinity, newTags,
            interactionCount, lastInteraction);
    }

    public Relationship recordInteraction(Instant when) {
        return new Relationship(from, to, trust, affinity, tags,
            interactionCount + 1, when);
    }

    public boolean hasTag(RelationshipTag tag) { return tags.contains(tag); }

    /** Combined social weight — used to bias affect in DialogueRequest. */
    public float socialWeight() {
        return (trust + affinity) * 0.5f;
    }
}
