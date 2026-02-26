package org.dynamisai.social;

import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SocialSystemTest {

    private DefaultSocialSystem social;
    private EntityId npc;
    private EntityId player;
    private FactionId guards;

    @BeforeEach
    void setUp() {
        social = new DefaultSocialSystem();
        npc = EntityId.of(1L);
        player = EntityId.of(2L);
        guards = FactionId.of("city-guards");
    }

    // ── FactionId ────────────────────────────────────────────────────────────

    @Test
    void factionIdRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> FactionId.of(""));
    }

    @Test
    void factionIdEquality() {
        assertEquals(FactionId.of("guards"), FactionId.of("guards"));
        assertNotEquals(FactionId.of("guards"), FactionId.of("thieves"));
    }

    // ── Relationship ─────────────────────────────────────────────────────────

    @Test
    void newRelationshipIsNeutral() {
        Relationship rel = social.graph().get(npc, player);
        assertTrue(rel.hasTag(RelationshipTag.NEUTRAL));
        assertEquals(0f, rel.trust(), 0.001f);
        assertEquals(0f, rel.affinity(), 0.001f);
    }

    @Test
    void adjustTrustModifiesBothEdges() {
        social.adjustTrust(npc, player, 0.4f);
        assertEquals(0.4f, social.graph().get(npc, player).trust(), 0.001f);
        assertEquals(0.4f, social.graph().get(player, npc).trust(), 0.001f);
    }

    @Test
    void trustClampsToOne() {
        social.adjustTrust(npc, player, 2.0f);
        assertEquals(1.0f, social.graph().get(npc, player).trust(), 0.001f);
    }

    @Test
    void trustClampsToMinusOne() {
        social.adjustTrust(npc, player, -2.0f);
        assertEquals(-1.0f, social.graph().get(npc, player).trust(), 0.001f);
    }

    @Test
    void tagRelationshipAddsTag() {
        social.tagRelationshipBoth(npc, player, RelationshipTag.ALLY);
        assertTrue(social.graph().get(npc, player).hasTag(RelationshipTag.ALLY));
        assertTrue(social.graph().get(player, npc).hasTag(RelationshipTag.ALLY));
    }

    @Test
    void addingTagRemovesNeutral() {
        social.tagRelationship(npc, player, RelationshipTag.ENEMY);
        assertFalse(social.graph().get(npc, player).hasTag(RelationshipTag.NEUTRAL));
        assertTrue(social.graph().get(npc, player).hasTag(RelationshipTag.ENEMY));
    }

    @Test
    void removingOnlyTagRestoresNeutral() {
        Relationship rel = Relationship.neutral(npc, player)
            .withTag(RelationshipTag.RIVAL)
            .withoutTag(RelationshipTag.RIVAL);
        assertTrue(rel.hasTag(RelationshipTag.NEUTRAL));
    }

    @Test
    void socialWeightIsAverageOfTrustAndAffinity() {
        Relationship r = Relationship.neutral(npc, player)
            .withTrust(0.8f).withAffinity(0.4f);
        assertEquals(0.6f, r.socialWeight(), 0.001f);
    }

    // ── FactionRegistry ───────────────────────────────────────────────────────

    @Test
    void unknownFactionStandingIsZero() {
        FactionStanding standing = social.factions().get(npc, guards);
        assertEquals(0f, standing.standing(), 0.001f);
        assertEquals("Stranger", standing.rank());
    }

    @Test
    void adjustFactionStandingModifiesScore() {
        social.adjustFactionStanding(npc, guards, 0.5f);
        assertEquals(0.5f, social.factions().get(npc, guards).standing(), 0.001f);
    }

    @Test
    void factionStandingClampsToOne() {
        social.adjustFactionStanding(npc, guards, 5.0f);
        assertEquals(1.0f, social.factions().get(npc, guards).standing(), 0.001f);
    }

    @Test
    void factionStandingIsHostileWhenNegative() {
        social.adjustFactionStanding(npc, guards, -0.5f);
        assertTrue(social.factions().get(npc, guards).isHostile());
    }

    @Test
    void addFactionPointsAccumulates() {
        social.addFactionPoints(npc, guards, 500L);
        social.addFactionPoints(npc, guards, 500L);
        assertEquals(1.0f, social.factions().get(npc, guards).standing(), 0.001f);
    }

    // ── DialogueHistory ───────────────────────────────────────────────────────

    @Test
    void recordDialogueIncreasesHistorySize() {
        social.recordDialogue(npc, player, "Hello.", "greeting", 0.5f, 1L);
        assertEquals(1, social.history().totalEntries());
    }

    @Test
    void recentReturnsNewestFirst() {
        social.recordDialogue(npc, player, "First.", "greeting", 0.5f, 1L);
        social.recordDialogue(npc, player, "Second.", "greeting", 0.3f, 2L);
        List<DialogueEntry> recent = social.history().recent(npc, player, 5);
        assertEquals("Second.", recent.get(0).text());
        assertEquals("First.", recent.get(1).text());
    }

    @Test
    void historyCapacityEvictsOldest() {
        DialogueHistory bounded = new DialogueHistory(3);
        for (int i = 0; i < 5; i++) {
            bounded.record(DialogueEntry.of(npc, player,
                "msg" + i, "t", 0f, i));
        }
        List<DialogueEntry> recent = bounded.recent(npc, player, 10);
        assertEquals(3, recent.size());
        assertEquals("msg4", recent.get(0).text());
    }

    @Test
    void meanSentimentCalculatedCorrectly() {
        social.recordDialogue(npc, player, "Good.", "t", 0.8f, 1L);
        social.recordDialogue(npc, player, "Bad.", "t", -0.4f, 2L);
        float mean = social.history().meanSentiment(npc, player, 5);
        assertEquals(0.2f, mean, 0.001f);
    }

    @Test
    void historyPairKeyIsOrderIndependent() {
        social.recordDialogue(npc, player, "Hi.", "greeting", 0.2f, 1L);
        List<DialogueEntry> forward = social.history().recent(npc, player, 5);
        List<DialogueEntry> reverse = social.history().recent(player, npc, 5);
        assertEquals(forward.size(), reverse.size());
    }

    @Test
    void positiveDialogueNudgesAffinity() {
        float before = social.graph().get(npc, player).affinity();
        social.recordDialogue(npc, player, "Wonderful!", "greeting", 0.9f, 1L);
        float after = social.graph().get(npc, player).affinity();
        assertTrue(after > before, "Positive dialogue must nudge affinity upward");
    }

    // ── SocialContext ─────────────────────────────────────────────────────────

    @Test
    void buildContextReturnsValidSnapshot() {
        SocialContext ctx = social.buildContext(npc, player);
        assertEquals(npc, ctx.npc());
        assertEquals(player, ctx.interlocutor());
        assertNotNull(ctx.relationship());
    }

    @Test
    void unknownEntityIsNotKnown() {
        SocialContext ctx = social.buildContext(npc, player);
        assertFalse(ctx.isKnownEntity());
    }

    @Test
    void entityIsKnownAfterDialogue() {
        social.recordDialogue(npc, player, "Hi.", "greeting", 0.5f, 1L);
        SocialContext ctx = social.buildContext(npc, player);
        assertTrue(ctx.isKnownEntity());
    }

    @Test
    void suppressThreatEscalationTrueForAlly() {
        social.tagRelationshipBoth(npc, player, RelationshipTag.ALLY);
        SocialContext ctx = social.buildContext(npc, player);
        assertTrue(ctx.suppressThreatEscalation());
    }

    @Test
    void sharedFactionDetected() {
        social.adjustFactionStanding(npc, guards, 0.5f);
        social.adjustFactionStanding(player, guards, 0.5f);
        SocialContext ctx = social.buildContext(npc, player);
        assertTrue(ctx.hasSharedFaction());
    }

    @Test
    void unknownFactoryBuildsNeutralContext() {
        SocialContext ctx = SocialContext.unknown(npc, player);
        assertEquals(0f, ctx.socialWeight(), 0.001f);
        assertFalse(ctx.isKnownEntity());
    }

    // ── SocialInfluence ───────────────────────────────────────────────────────

    @Test
    void neutralContextProducesNeutralInfluence() {
        SocialInfluence inf = social.influence(npc, player);
        assertEquals(1.0f, inf.temperatureModifier(), 0.001f);
        assertFalse(inf.forceCompliance());
        assertFalse(inf.allowPersonalTopics());
    }

    @Test
    void enemyTagReducesTemperature() {
        social.tagRelationship(npc, player, RelationshipTag.ENEMY);
        SocialInfluence inf = social.influence(npc, player);
        assertTrue(inf.temperatureModifier() < 1.0f);
    }

    @Test
    void indebtedTagForcesCompliance() {
        social.tagRelationship(npc, player, RelationshipTag.INDEBTED);
        SocialInfluence inf = social.influence(npc, player);
        assertTrue(inf.forceCompliance());
    }

    @Test
    void trustedTagAllowsPersonalTopics() {
        social.tagRelationship(npc, player, RelationshipTag.TRUSTED);
        SocialInfluence inf = social.influence(npc, player);
        assertTrue(inf.allowPersonalTopics());
    }

    @Test
    void trustedTagRaisesTemperature() {
        social.tagRelationship(npc, player, RelationshipTag.TRUSTED);
        SocialInfluence inf = social.influence(npc, player);
        assertTrue(inf.temperatureModifier() > 1.0f);
    }

    // ── Remove entity ─────────────────────────────────────────────────────────

    @Test
    void removeEntityClearsSocialGraph() {
        social.adjustTrust(npc, player, 0.5f);
        social.removeEntity(npc);
        assertEquals(0f, social.graph().get(npc, player).trust(), 0.001f);
    }

    @Test
    void removeEntityClearsFactionStandings() {
        social.adjustFactionStanding(npc, guards, 0.8f);
        social.removeEntity(npc);
        assertEquals(0f, social.factions().get(npc, guards).standing(), 0.001f);
    }
}
