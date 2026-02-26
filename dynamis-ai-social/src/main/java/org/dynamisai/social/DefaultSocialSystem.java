package org.dynamisai.social;

import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default SocialSystem implementation.
 *
 * Wires SocialGraph + FactionRegistry + DialogueHistory.
 * All state is in the three subsystems — DefaultSocialSystem is stateless itself.
 * Thread-safe: subsystems are individually thread-safe.
 */
public final class DefaultSocialSystem implements SocialSystem {

    private static final Logger log = LoggerFactory.getLogger(DefaultSocialSystem.class);

    /** Small affinity bump applied on any positive dialogue. */
    private static final float DIALOGUE_AFFINITY_NUDGE = 0.02f;

    /** Small affinity penalty on negative dialogue. */
    private static final float DIALOGUE_AFFINITY_PENALTY = 0.03f;

    /** Number of recent history entries included in SocialContext. */
    private static final int HISTORY_WINDOW = 5;

    private final SocialGraph socialGraph;
    private final FactionRegistry factionRegistry;
    private final DialogueHistory dialogueHistory;

    public DefaultSocialSystem() {
        this(new SocialGraph(), new FactionRegistry(), new DialogueHistory());
    }

    public DefaultSocialSystem(SocialGraph socialGraph,
                                FactionRegistry factionRegistry,
                                DialogueHistory dialogueHistory) {
        this.socialGraph = socialGraph;
        this.factionRegistry = factionRegistry;
        this.dialogueHistory = dialogueHistory;
    }

    @Override
    public SocialContext buildContext(EntityId npc, EntityId interlocutor) {
        Relationship rel = socialGraph.get(npc, interlocutor);
        Relationship reverseRel = socialGraph.get(interlocutor, npc);

        List<FactionStanding> npcFactions = factionRegistry.standingsFor(npc);
        List<FactionStanding> theirFactions =
            factionRegistry.standingsFor(interlocutor);

        List<DialogueEntry> history =
            dialogueHistory.recent(npc, interlocutor, HISTORY_WINDOW);

        boolean hasSharedFaction = npcFactions.stream()
            .filter(FactionStanding::isFriendly)
            .anyMatch(fs -> theirFactions.stream()
                .filter(FactionStanding::isFriendly)
                .anyMatch(tf -> tf.faction().equals(fs.faction())));

        boolean isKnownEntity = rel.interactionCount() > 0 ||
                                !history.isEmpty();

        float socialWeight = rel.socialWeight();

        return new SocialContext(
            npc, interlocutor,
            rel, reverseRel,
            List.copyOf(npcFactions),
            List.copyOf(history),
            socialWeight,
            hasSharedFaction,
            isKnownEntity
        );
    }

    @Override
    public SocialInfluence influence(EntityId npc, EntityId interlocutor) {
        return SocialInfluence.from(buildContext(npc, interlocutor));
    }

    @Override
    public void recordDialogue(EntityId speaker, EntityId listener,
                                String text, String topic,
                                float sentiment, long tick) {
        DialogueEntry entry = DialogueEntry.of(
            speaker, listener, text, topic, sentiment, tick);
        dialogueHistory.record(entry);

        // Nudge affinity based on sentiment
        if (sentiment > 0.1f) {
            socialGraph.adjustAffinity(speaker, listener,
                DIALOGUE_AFFINITY_NUDGE * sentiment);
        } else if (sentiment < -0.1f) {
            socialGraph.adjustAffinity(speaker, listener,
                DIALOGUE_AFFINITY_PENALTY * sentiment);
        }

        log.debug("Dialogue recorded: {}→{} topic={} sentiment={}",
            speaker, listener, topic, sentiment);
    }

    @Override
    public void adjustTrust(EntityId a, EntityId b, float delta) {
        socialGraph.adjustTrust(a, b, delta);
    }

    @Override
    public void adjustAffinity(EntityId a, EntityId b, float delta) {
        socialGraph.adjustAffinity(a, b, delta);
    }

    @Override
    public void tagRelationship(EntityId from, EntityId to, RelationshipTag tag) {
        socialGraph.addTag(from, to, tag);
    }

    @Override
    public void tagRelationshipBoth(EntityId a, EntityId b, RelationshipTag tag) {
        socialGraph.addTagBoth(a, b, tag);
    }

    @Override
    public void adjustFactionStanding(EntityId entity, FactionId faction,
                                       float delta) {
        factionRegistry.adjust(entity, faction, delta);
    }

    @Override
    public void addFactionPoints(EntityId entity, FactionId faction, long points) {
        factionRegistry.addPoints(entity, faction, points);
    }

    @Override
    public void removeEntity(EntityId entity) {
        socialGraph.removeEntity(entity);
        factionRegistry.removeEntity(entity);
        log.debug("Social state removed for entity {}", entity);
    }

    @Override public SocialGraph graph() { return socialGraph; }
    @Override public FactionRegistry factions() { return factionRegistry; }
    @Override public DialogueHistory history() { return dialogueHistory; }
}
