package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;

import java.time.Duration;

/**
 * Sealed event hierarchy — all data flowing from DynamisAI to Animis.
 * Animis consumes these; DynamisAI produces them.
 */
public sealed interface AnimisBridgeEvent
    permits AnimisBridgeEvent.VoiceJobEvent,
            AnimisBridgeEvent.IntentEvent,
            AnimisBridgeEvent.AffectEvent {

    EntityId agent();

    /** A completed VoiceRenderJob ready for playback and lip-sync. */
    record VoiceJobEvent(
        EntityId agent,
        VoiceRenderJob job
    ) implements AnimisBridgeEvent {}

    /**
     * Predictive intent signal — submit this anticipateBy before the action fires.
     * Default anticipation: 180ms (one motion-matching blend window).
     */
    record IntentEvent(
        EntityId agent,
        IntentSignal intent,
        Duration anticipateBy
    ) implements AnimisBridgeEvent {
        public static IntentEvent withDefaultAnticipation(EntityId agent, IntentSignal intent) {
            return new IntentEvent(agent, intent, Duration.ofMillis(180));
        }
    }

    /**
     * Continuous affect update — drives facial rig blending.
     * Submitted from deliberative tick (~1 Hz), not per-frame.
     */
    record AffectEvent(
        EntityId agent,
        AffectVector affect
    ) implements AnimisBridgeEvent {}
}
