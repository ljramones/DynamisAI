package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;

import java.time.Duration;
import java.util.List;

/**
 * DynamisAI → Animis boundary contract.
 *
 * DynamisAI owns the production side of this interface.
 * Animis owns the consumption side.
 * Neither reaches into the other's internals.
 */
public interface AnimisBridge {

    /**
     * Submit a completed VoiceRenderJob for animation consumption.
     * Animis begins blending toward the first viseme at
     * (job.estimatedDuration() - ANIMIS_PREROLL_MS) before audio starts.
     */
    void submitVoiceJob(VoiceRenderJob job);

    /**
     * Submit a predictive intent signal before the action commits.
     * Animis begins motion-matching blend anticipateBy before the action triggers.
     *
     * @param agent The NPC whose animation rig to target
     * @param intent The predicted action
     * @param anticipateBy How far in advance to begin the blend (default 180ms)
     */
    void submitIntentSignal(EntityId agent, IntentSignal intent, Duration anticipateBy);

    /**
     * Push the current AffectVector to drive continuous facial rig blending.
     * Called from deliberative tick (~1 Hz) — not per-frame.
     */
    void pushAffectState(EntityId agent, AffectVector affect);

    /**
     * Retrieve all pending events for this agent since last poll.
     * Animis calls this each frame to consume queued events.
     */
    List<AnimisBridgeEvent> pollEvents(EntityId agent);

    /** Clear all queued events for an agent — called on NPC despawn. */
    void clearEvents(EntityId agent);
}
