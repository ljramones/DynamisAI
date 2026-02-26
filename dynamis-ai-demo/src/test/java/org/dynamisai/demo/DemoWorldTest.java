package org.dynamisai.demo;

import org.dynamisai.social.RelationshipTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoWorldTest {

    private DemoWorld world;

    @BeforeEach
    void setUp() {
        world = new DemoWorld();
    }

    @AfterEach
    void tearDown() {
        world.shutdown();
    }

    @Test
    void worldInitialisesWithThreeNpcs() {
        assertNotNull(world.guard1);
        assertNotNull(world.guard2);
        assertNotNull(world.player);
    }

    @Test
    void approachMovesPlayerCloser() {
        float distBefore = world.guard1.distanceTo(world.player.position);
        world.tick(1L, PlayerAction.APPROACH, "");
        float distAfter = world.guard1.distanceTo(world.player.position);
        assertTrue(distAfter < distBefore, "APPROACH must reduce distance to guards");
    }

    @Test
    void fleeMovesPlayerAway() {
        for (int i = 0; i < 5; i++) {
            world.tick(i + 1L, PlayerAction.APPROACH, "");
        }
        float distBefore = world.guard1.distanceTo(world.player.position);
        world.tick(6L, PlayerAction.FLEE, "");
        float distAfter = world.guard1.distanceTo(world.player.position);
        assertTrue(distAfter > distBefore, "FLEE must increase distance from guards");
    }

    @Test
    void hostileActTagsEnemyRelationship() {
        world.tick(1L, PlayerAction.HOSTILE, "");
        var rel = world.social.graph().get(world.guard1.id, world.player.id);
        assertTrue(rel.hasTag(RelationshipTag.ENEMY), "First hostile act must tag ENEMY");
    }

    @Test
    void waitDoesNotMovePlayer() {
        var posBefore = world.player.position;
        world.tick(1L, PlayerAction.WAIT, "");
        var posAfter = world.player.position;
        assertEquals(posBefore.x(), posAfter.x(), 0.001);
        assertEquals(posBefore.z(), posAfter.z(), 0.001);
    }

    @Test
    void tickReturnsNonNullRecord() {
        TickRecord record = world.tick(1L, PlayerAction.WAIT, "");
        assertNotNull(record);
        assertEquals(1L, record.tick());
    }

    @Test
    void crowdGroupHasTwoGuards() {
        world.tick(1L, PlayerAction.WAIT, "");
        assertEquals(2, world.crowd.totalAgents());
    }

    @Test
    void reportAccumulatesTickRecords() {
        world.tick(1L, PlayerAction.WAIT, "");
        world.tick(2L, PlayerAction.WAIT, "");
        world.tick(3L, PlayerAction.WAIT, "");
        assertDoesNotThrow(() -> {
            Path tmp = Files.createTempFile("demo-test", ".json");
            world.report().writeJson(tmp);
            String json = Files.readString(tmp);
            assertTrue(json.contains("\"totalTicks\": 3"));
        });
    }

    @Test
    void threeHostileActsProduceBetrayedTag() {
        for (int i = 0; i < 3; i++) {
            world.tick(i + 1L, PlayerAction.HOSTILE, "");
        }
        var rel = world.social.graph().get(world.guard1.id, world.player.id);
        assertTrue(rel.hasTag(RelationshipTag.BETRAYED));
    }

    @Test
    void speechInputFlowsToDialogueRequest() {
        for (int i = 0; i < 7; i++) {
            world.tick(i + 1L, PlayerAction.APPROACH, "");
        }
        TickRecord record = world.tick(8L, PlayerAction.SPEAK, "I come in peace");
        assertNotNull(record.playerSpeech());
    }
}
