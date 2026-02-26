package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.util.List;
import java.util.Optional;

/**
 * Snapshot of social context for an NPC at a given tick.
 */
public record SocialContext(
    EntityId npc,
    EntityId interlocutor,
    Relationship relationship,
    Relationship reverseRelationship,
    List<FactionStanding> npcFactions,
    List<DialogueEntry> recentHistory,
    float socialWeight,
    boolean hasSharedFaction,
    boolean isKnownEntity
) {
    /**
     * Affect bias to apply to AffectVector.valence() — positive for friendly,
     * negative for hostile relationships.
     */
    public float affinityBias() {
        return Math.max(-0.5f, Math.min(0.5f, socialWeight * 0.5f));
    }

    /**
     * Whether this context should suppress threat-escalation dialogue.
     */
    public boolean suppressThreatEscalation() {
        return relationship.trust() > 0.5f ||
               relationship.hasTag(RelationshipTag.ALLY);
    }

    /**
     * Whether the NPC should volunteer information it normally withholds.
     */
    public boolean shouldShareInformation() {
        return relationship.hasTag(RelationshipTag.TRUSTED) ||
               relationship.trust() > 0.6f;
    }

    /**
     * Whether the NPC should comply with requests without argument.
     */
    public boolean shouldComplyReadily() {
        return relationship.hasTag(RelationshipTag.FEARED) ||
               relationship.hasTag(RelationshipTag.INDEBTED);
    }

    /** Most recent dialogue entry between this pair, if any. */
    public Optional<DialogueEntry> lastDialogue() {
        return recentHistory.isEmpty()
            ? Optional.empty()
            : Optional.of(recentHistory.get(0));
    }

    /** A neutral context — used when no social data is available. */
    public static SocialContext unknown(EntityId npc, EntityId interlocutor) {
        return new SocialContext(
            npc, interlocutor,
            Relationship.neutral(npc, interlocutor),
            Relationship.neutral(interlocutor, npc),
            List.of(), List.of(),
            0f, false, false
        );
    }
}
