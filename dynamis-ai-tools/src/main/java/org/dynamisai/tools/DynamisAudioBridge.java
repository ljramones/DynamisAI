package org.dynamisai.tools;

import io.dynamis.audio.api.EmitterImportance;
import io.dynamis.audio.core.LogicalEmitter;
import io.dynamis.audio.core.VoiceManager;
import org.dynamisai.core.EntityId;
import org.dynamisai.voice.VoiceRenderJob;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges VoiceRenderJob output to DynamisAudio emitters.
 */
public final class DynamisAudioBridge {

    private final VoiceManager voiceManager;
    private final Map<EntityId, LogicalEmitter> activeEmitters = new ConcurrentHashMap<>();

    public DynamisAudioBridge(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    public void submitVoiceJob(VoiceRenderJob job, float x, float y, float z) {
        if (job == null || job.primarySpeech() == null || job.primarySpeech().pcmData().length == 0) {
            return;
        }

        // Replace any existing emitter for this NPC.
        destroyEmitters(job.speaker());

        LogicalEmitter emitter = new LogicalEmitter("npc-" + job.speaker().value(), EmitterImportance.NORMAL);
        emitter.setPosition(x, y, z);

        // Materialize an audio asset so bridge code exercises stream conversion path.
        new VoiceRenderJobAudioAsset(job.primarySpeech());

        voiceManager.register(emitter);
        activeEmitters.put(job.speaker(), emitter);
    }

    public int activeEmitterCount() {
        return activeEmitters.size();
    }

    public void destroyEmitters(EntityId npc) {
        LogicalEmitter emitter = activeEmitters.remove(npc);
        if (emitter == null) {
            return;
        }
        voiceManager.unregister(emitter);
        emitter.destroy();
    }

    public void updateEmitterPosition(EntityId npc, float x, float y, float z) {
        LogicalEmitter emitter = activeEmitters.get(npc);
        if (emitter != null) {
            emitter.setPosition(x, y, z);
        }
    }

    public boolean isAvailable() {
        return true;
    }
}
