package org.dynamisai.planning;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SquadBlackboardTest {

    @Test
    void writeThenReadDirectReturnsValue() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.RADIO, 1L);
        assertEquals("v", bb.readDirect("k").orElseThrow());
    }

    @Test
    void radioReadIgnoresPositions() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.RADIO, 1L);
        assertTrue(bb.read("k", EntityId.of(2L), new Location(999,0,999), new Location(0,0,0), 1L).isPresent());
    }

    @Test
    void losWithinThresholdReadable() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.LINE_OF_SIGHT, 1L);
        assertTrue(bb.read("k", EntityId.of(2L), new Location(0,0,0), new Location(10,0,0), 1L).isPresent());
    }

    @Test
    void losBeyondThresholdUnreadable() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.LINE_OF_SIGHT, 1L);
        assertTrue(bb.read("k", EntityId.of(2L), new Location(0,0,0), new Location(30,0,0), 1L).isEmpty());
    }

    @Test
    void runnerInsufficientTicksUnreadable() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.RUNNER, 1L);
        assertTrue(bb.read("k", EntityId.of(2L), new Location(20,0,0), new Location(0,0,0), 2L).isEmpty());
    }

    @Test
    void runnerSufficientTicksReadable() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.write("k", "v", EntityId.of(1L), PropagationChannel.RUNNER, 1L);
        assertTrue(bb.read("k", EntityId.of(2L), new Location(20,0,0), new Location(0,0,0), 5L).isPresent());
    }

    @Test
    void retractRemovesEntry() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("k", "v", EntityId.of(1L), 1L);
        bb.retract("k");
        assertTrue(bb.readDirect("k").isEmpty());
    }

    @Test
    void pruneStaleRemovesOldEntries() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("old", "v", EntityId.of(1L), 1L);
        bb.writeRadio("new", "v", EntityId.of(1L), 10L);
        bb.pruneStale(20L, 10L);
        assertTrue(bb.readDirect("old").isEmpty());
        assertTrue(bb.readDirect("new").isPresent());
    }

    @Test
    void entryCountReflectsLifecycle() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("a", 1, EntityId.of(1L), 1L);
        bb.writeRadio("b", 2, EntityId.of(1L), 1L);
        assertEquals(2, bb.entryCount());
        bb.retract("a");
        assertEquals(1, bb.entryCount());
    }

    @Test
    void writeRadioImmediatelyReadable() {
        SquadBlackboard bb = new SquadBlackboard("alpha");
        bb.writeRadio("instant", true, EntityId.of(1L), 1L);
        assertTrue(bb.read("instant", EntityId.of(9L),
            new Location(1000,0,1000), new Location(-1000,0,-1000), 1L).isPresent());
    }
}
