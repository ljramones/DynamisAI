package org.dynamisai.social;

import org.dynamis.core.entity.EntityId;

/**
 * Social system interface — manages relationship graph, faction standings,
 * and dialogue history for all NPCs.
 */
public interface SocialSystem {

    /**
     * Build a SocialContext snapshot for npc↔interlocutor at the current tick.
     * Always returns a valid context — returns SocialContext.unknown() if no data.
     */
    SocialContext buildContext(EntityId npc, EntityId interlocutor);

    /**
     * Derive SocialInfluence modifiers from current context.
     * Convenience wrapper around buildContext + SocialInfluence.from().
     */
    SocialInfluence influence(EntityId npc, EntityId interlocutor);

    /**
     * Record a completed dialogue turn.
     * Updates DialogueHistory and adjusts relationship affinity slightly.
     */
    void recordDialogue(EntityId speaker, EntityId listener,
                        String text, String topic,
                        float sentiment, long tick);

    /** Adjust trust between two entities. Delta in [-1, 1], clamped. */
    void adjustTrust(EntityId a, EntityId b, float delta);

    /** Adjust affinity between two entities. Delta in [-1, 1], clamped. */
    void adjustAffinity(EntityId a, EntityId b, float delta);

    /** Apply a relationship tag to the a→b directed edge. */
    void tagRelationship(EntityId from, EntityId to, RelationshipTag tag);

    /** Apply a tag to both directed edges. */
    void tagRelationshipBoth(EntityId a, EntityId b, RelationshipTag tag);

    /** Adjust faction standing for an entity. */
    void adjustFactionStanding(EntityId entity, FactionId faction, float delta);

    /** Add faction contribution points — recalculates standing. */
    void addFactionPoints(EntityId entity, FactionId faction, long points);

    /** Remove all social data for an entity — call on despawn. */
    void removeEntity(EntityId entity);

    /**
     * Advance social simulation by one tick.
     * Drives rumor propagation.
     *
     * @param currentTick current simulation tick
     */
    void tick(long currentTick);

    /** Expose the underlying SocialGraph — for diagnostics and tools. */
    SocialGraph graph();

    /** Expose the FactionRegistry — for diagnostics and tools. */
    FactionRegistry factions();

    /** Expose DialogueHistory — for diagnostics and tools. */
    DialogueHistory history();

    /** Event-driven relationship delta engine. */
    ReputationEngine reputationEngine();

    /** Rumor propagation runtime for second-hand social updates. */
    RumorPropagator rumorPropagator();

    /** Daily schedule and needs runtime. */
    ScheduleEngine scheduleEngine();

    /** Pacing-layer drama output manager. */
    DramaManager dramaManager();

    /** Engagement signal accumulator used by DramaManager. */
    EngagementTracker engagementTracker();
}
