package org.dynamisai.tools;

import io.vavr.collection.HashMap;
import org.dynamisai.core.AIOutputFrame;
import org.dynamisai.core.DialogueEvent;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EnvironmentState;
import org.dynamisai.core.FrameBudgetReport;
import org.dynamisai.core.GlobalFacts;
import org.dynamisai.core.Location;
import org.dynamisai.core.SteeringOutput;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonBakeAssertionsTest {

    @Test
    void steeringOutputNotNullPassesAndFails() {
        BakeAssertion a = CommonBakeAssertions.steeringOutputNotNull();
        assertTrue(a.evaluate(1, frame(Map.of(), List.of())).isEmpty());
        AIOutputFrame bad = new AIOutputFrame(1, 1, null, List.of(), List.of(), emptySnapshot(), FrameBudgetReport.empty(1));
        assertTrue(a.evaluate(1, bad).isPresent());
    }

    @Test
    void noBlankDialoguePassesAndFails() {
        BakeAssertion a = CommonBakeAssertions.noBlankDialogue();
        EntityId npc = EntityId.of(1);
        assertTrue(a.evaluate(1, frame(Map.of(), List.of(DialogueEvent.of(npc, null, "hello", "neutral")))).isEmpty());
        assertTrue(a.evaluate(1, frame(Map.of(), List.of(DialogueEvent.of(npc, null, "   ", "neutral")))).isPresent());
    }

    @Test
    void maxSpeedPassesAndFails() {
        BakeAssertion a = CommonBakeAssertions.maxSpeed(10f);
        EntityId npc = EntityId.of(1);
        assertTrue(a.evaluate(1, frame(Map.of(npc, new SteeringOutput(new Location(3, 0, 4), new Location(0, 0, 1), 1, false)), List.of())).isEmpty());
        assertTrue(a.evaluate(1, frame(Map.of(npc, new SteeringOutput(new Location(11, 0, 0), new Location(0, 0, 1), 1, false)), List.of())).isPresent());
    }

    @Test
    void dialogueRateLimitFailsWhenSpeakerExceeds() {
        BakeAssertion a = CommonBakeAssertions.dialogueRateLimit(1);
        EntityId npc = EntityId.of(1);
        AIOutputFrame frame = frame(Map.of(), List.of(
            DialogueEvent.of(npc, null, "one", "n"),
            DialogueEvent.of(npc, null, "two", "n")
        ));
        assertTrue(a.evaluate(1, frame).isPresent());
    }

    @Test
    void noMassStallRequiresConsecutiveTicks() {
        BakeAssertion a = CommonBakeAssertions.noMassStall(0.8f, 2);
        EntityId a1 = EntityId.of(1);
        EntityId a2 = EntityId.of(2);

        AIOutputFrame stalled = frame(Map.of(
            a1, SteeringOutput.stopped(),
            a2, SteeringOutput.stopped()
        ), List.of());

        assertFalse(a.evaluate(1, stalled).isPresent());
        assertTrue(a.evaluate(2, stalled).isPresent());
    }

    private static AIOutputFrame frame(Map<EntityId, SteeringOutput> steering, List<DialogueEvent> dialogue) {
        return new AIOutputFrame(1, 1, steering, dialogue, List.of(), emptySnapshot(), FrameBudgetReport.empty(1));
    }

    private static WorldSnapshot emptySnapshot() {
        return new WorldSnapshot(1L, HashMap.empty(), new GlobalFacts(Map.of()), new EnvironmentState("clear", 12f, 1f));
    }
}
