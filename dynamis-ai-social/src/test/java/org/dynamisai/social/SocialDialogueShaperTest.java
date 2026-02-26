package org.dynamisai.social;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueRequest;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SocialDialogueShaperTest {

    private DefaultSocialSystem social;
    private EntityId npc;
    private EntityId player;

    @BeforeEach
    void setUp() {
        social = new DefaultSocialSystem();
        npc = EntityId.of(1L);
        player = EntityId.of(2L);
    }

    private DialogueRequest makeRequest(float valence) {
        return new DialogueRequest(
            npc, player, "Hello.",
            new WorldFacts(Map.of(), List.of(), ThreatLevel.NONE,
                new Location(0, 0, 0), null),
            new AffectVector(valence, 0.5f, 0.4f, 0.0f, 0.5f),
            null);
    }

    @Test
    void shapeReturnsNewInstance() {
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertNotSame(original, shaped);
    }

    @Test
    void shapePreservesOriginalSpeakerAndTarget() {
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0f), social);
        assertEquals(npc, shaped.speaker());
        assertEquals(player, shaped.target());
    }

    @Test
    void noHistoryProducesUnchangedInput() {
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertEquals(original.inputSpeech(), shaped.inputSpeech());
    }

    @Test
    void allyTagPrependsHintToInput() {
        social.tagRelationshipBoth(npc, player, RelationshipTag.ALLY);
        social.recordDialogue(npc, player, "Hi.", "greeting", 0.5f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0f), social);
        assertTrue(shaped.inputSpeech().startsWith("["));
        assertTrue(shaped.inputSpeech().contains("ally"));
    }

    @Test
    void enemyTagPrependsHint() {
        social.tagRelationship(npc, player, RelationshipTag.ENEMY);
        social.recordDialogue(npc, player, "Back off.", "threat", -0.8f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0f), social);
        assertTrue(shaped.inputSpeech().contains("enemy"));
    }

    @Test
    void positiveAffinityBiasesValenceUpward() {
        social.adjustAffinity(npc, player, 0.8f);
        social.recordDialogue(npc, player, "Hello.", "greeting", 0.5f, 1L);
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertTrue(shaped.currentMood().valence() > original.currentMood().valence());
    }

    @Test
    void negativeAffinityBiasesValenceDownward() {
        social.adjustAffinity(npc, player, -0.8f);
        social.recordDialogue(npc, player, "Go away.", "hostility", -0.7f, 1L);
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertTrue(shaped.currentMood().valence() < original.currentMood().valence());
    }

    @Test
    void valenceClampedToOne() {
        social.adjustAffinity(npc, player, 1.0f);
        social.recordDialogue(npc, player, "!", "t", 1.0f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0.9f), social);
        assertTrue(shaped.currentMood().valence() <= 1.0f);
    }

    @Test
    void valenceClampedToMinusOne() {
        social.adjustAffinity(npc, player, -1.0f);
        social.recordDialogue(npc, player, "!", "t", -1.0f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(-0.9f), social);
        assertTrue(shaped.currentMood().valence() >= -1.0f);
    }

    @Test
    void trustBiasesDominance() {
        social.adjustTrust(npc, player, 0.6f);
        social.recordDialogue(npc, player, "Trust.", "t", 0.5f, 1L);
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertTrue(shaped.currentMood().dominance() >= original.currentMood().dominance());
    }

    @Test
    void indebtedTagAppendsComplyHint() {
        social.tagRelationship(npc, player, RelationshipTag.INDEBTED);
        social.recordDialogue(npc, player, "As you wish.", "t", 0.3f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0f), social);
        assertTrue(shaped.inputSpeech().contains("comply"));
    }

    @Test
    void lastDialogueTopicAppearsInHint() {
        social.tagRelationshipBoth(npc, player, RelationshipTag.TRUSTED);
        social.recordDialogue(npc, player, "About the quest.", "quest:main", 0.5f, 1L);
        DialogueRequest shaped = SocialDialogueShaper.shape(makeRequest(0f), social);
        assertTrue(shaped.inputSpeech().contains("quest:main"));
    }

    @Test
    void arousalAndSarcasmUnchangedByShaping() {
        social.adjustAffinity(npc, player, 0.5f);
        social.recordDialogue(npc, player, "Hi.", "t", 0.5f, 1L);
        DialogueRequest original = makeRequest(0f);
        DialogueRequest shaped = SocialDialogueShaper.shape(original, social);
        assertEquals(original.currentMood().arousal(), shaped.currentMood().arousal(), 0.001f);
        assertEquals(original.currentMood().sarcasm(), shaped.currentMood().sarcasm(), 0.001f);
    }
}
