package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimisBridgeTest {

    private DefaultAnimisBridge bridge;
    private EntityId agent;

    @BeforeEach
    void setUp() {
        bridge = new DefaultAnimisBridge();
        agent = EntityId.of(1L);
    }

    private VoiceRenderJob stubJob() {
        return VoiceRenderJob.stub(agent, "Test speech.");
    }

    @Test
    void submitVoiceJobProducesVoiceJobEvent() {
        bridge.submitVoiceJob(stubJob());
        List<AnimisBridgeEvent> events = bridge.pollEvents(agent);
        assertEquals(1, events.size());
        assertInstanceOf(AnimisBridgeEvent.VoiceJobEvent.class, events.get(0));
    }

    @Test
    void voiceJobEventContainsCorrectJob() {
        VoiceRenderJob job = stubJob();
        bridge.submitVoiceJob(job);
        var event = (AnimisBridgeEvent.VoiceJobEvent) bridge.pollEvents(agent).get(0);
        assertEquals(job, event.job());
    }

    @Test
    void submitIntentSignalProducesIntentEvent() {
        bridge.submitIntentSignal(agent, IntentSignal.certain(IntentType.FLEE),
            Duration.ofMillis(180));
        List<AnimisBridgeEvent> events = bridge.pollEvents(agent);
        assertEquals(1, events.size());
        assertInstanceOf(AnimisBridgeEvent.IntentEvent.class, events.get(0));
    }

    @Test
    void intentEventHasCorrectAnticipation() {
        Duration anticipation = Duration.ofMillis(180);
        bridge.submitIntentSignal(agent, IntentSignal.certain(IntentType.CROUCH), anticipation);
        var event = (AnimisBridgeEvent.IntentEvent) bridge.pollEvents(agent).get(0);
        assertEquals(anticipation, event.anticipateBy());
    }

    @Test
    void pushAffectStateProducesAffectEvent() {
        bridge.pushAffectState(agent, AffectVector.fearful());
        List<AnimisBridgeEvent> events = bridge.pollEvents(agent);
        assertEquals(1, events.size());
        assertInstanceOf(AnimisBridgeEvent.AffectEvent.class, events.get(0));
    }

    @Test
    void affectEventContainsCorrectAffect() {
        bridge.pushAffectState(agent, AffectVector.fearful());
        var event = (AnimisBridgeEvent.AffectEvent) bridge.pollEvents(agent).get(0);
        assertEquals(AffectVector.fearful(), event.affect());
    }

    @Test
    void pollEventsDrainsQueue() {
        bridge.submitVoiceJob(stubJob());
        bridge.pollEvents(agent);
        assertTrue(bridge.pollEvents(agent).isEmpty());
    }

    @Test
    void pollEventsReturnsEmptyForUnknownAgent() {
        assertTrue(bridge.pollEvents(EntityId.of(999L)).isEmpty());
    }

    @Test
    void multipleEventsQueueAndDrainInOrder() {
        bridge.submitVoiceJob(stubJob());
        bridge.submitIntentSignal(agent, IntentSignal.certain(IntentType.IDLE),
            Duration.ofMillis(180));
        bridge.pushAffectState(agent, AffectVector.neutral());

        List<AnimisBridgeEvent> events = bridge.pollEvents(agent);
        assertEquals(3, events.size());
        assertInstanceOf(AnimisBridgeEvent.VoiceJobEvent.class, events.get(0));
        assertInstanceOf(AnimisBridgeEvent.IntentEvent.class, events.get(1));
        assertInstanceOf(AnimisBridgeEvent.AffectEvent.class, events.get(2));
    }

    @Test
    void clearEventsRemovesAllQueuedEvents() {
        bridge.submitVoiceJob(stubJob());
        bridge.submitVoiceJob(stubJob());
        bridge.clearEvents(agent);
        assertTrue(bridge.pollEvents(agent).isEmpty());
    }

    @Test
    void differentAgentsHaveIndependentQueues() {
        EntityId other = EntityId.of(2L);
        bridge.submitVoiceJob(stubJob());
        bridge.pushAffectState(other, AffectVector.angry());

        assertEquals(1, bridge.pollEvents(agent).size());
        assertEquals(1, bridge.pollEvents(other).size());
    }

    @Test
    void intentSignalRejectsConfidenceOutOfRange() {
        assertThrows(IllegalArgumentException.class, () ->
            new IntentSignal(IntentType.FLEE, 1.5f, Direction.none()));
    }

    @Test
    void directionNormalizesCorrectly() {
        Direction d = new Direction(3f, 0f, 4f).normalized();
        assertEquals(0.6f, d.x(), 0.001f);
        assertEquals(0.8f, d.z(), 0.001f);
    }

    @Test
    void directionZeroNormalizesToNone() {
        Direction d = new Direction(0f, 0f, 0f).normalized();
        assertEquals(Direction.none(), d);
    }

    @Test
    void defaultAnticipationIs180ms() {
        var event = AnimisBridgeEvent.IntentEvent.withDefaultAnticipation(
            agent, IntentSignal.certain(IntentType.ATTACK_LEFT));
        assertEquals(180, event.anticipateBy().toMillis());
    }

    @Test
    void patternMatchingOnEventHierarchyIsExhaustive() {
        bridge.submitVoiceJob(stubJob());
        bridge.submitIntentSignal(agent, IntentSignal.certain(IntentType.SPRINT),
            Duration.ofMillis(180));
        bridge.pushAffectState(agent, AffectVector.content());

        for (AnimisBridgeEvent event : bridge.pollEvents(agent)) {
            String result = switch (event) {
                case AnimisBridgeEvent.VoiceJobEvent e -> "voice";
                case AnimisBridgeEvent.IntentEvent e -> "intent";
                case AnimisBridgeEvent.AffectEvent e -> "affect";
            };
            assertNotNull(result);
        }
    }
}
