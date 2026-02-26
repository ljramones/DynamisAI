package org.dynamisai.tools;

import io.dynamis.audio.core.VoiceManager;
import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.core.EntityId;
import org.dynamisai.voice.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AudioBridgeTest {

    private DynamisAudioBridge bridge;
    private VoiceManager voiceManager;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        voiceManager = new VoiceManager(32, 4);
        bridge = new DynamisAudioBridge(voiceManager);
        npc = EntityId.of(1L);
    }

    private VoiceRenderJob stubJobWithPcm() {
        float[] samples = new float[4800];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (float) Math.sin(2 * Math.PI * 440.0 * i / 48000.0) * 0.3f;
        }

        AudioStream primary = AudioStream.fromPcmFloats("primary", samples, 24000);
        AudioStream nonverbal = AudioStream.empty("nv");

        return new VoiceRenderJob(
            npc,
            primary,
            nonverbal,
            List.of(new VisemeTimestamp(Duration.ZERO, "AH", 0.8f)),
            AffectVector.neutral(),
            PhysicalVoiceContext.calm(),
            Duration.ofMillis(200)
        );
    }

    @Test
    void submitVoiceJobRegistersEmitterWithVoiceManager() {
        bridge.submitVoiceJob(stubJobWithPcm(), 0f, 0f, 0f);
        assertTrue(voiceManager.getRegisteredCount() > 0);
    }

    @Test
    void submitVoiceJobCreatesActiveEmitter() {
        bridge.submitVoiceJob(stubJobWithPcm(), 5f, 0f, 3f);
        assertEquals(1, bridge.activeEmitterCount());
    }

    @Test
    void destroyEmittersRemovesFromActiveMap() {
        bridge.submitVoiceJob(stubJobWithPcm(), 0f, 0f, 0f);
        bridge.destroyEmitters(npc);
        assertEquals(0, bridge.activeEmitterCount());
    }

    @Test
    void destroyEmittersIsIdempotent() {
        bridge.submitVoiceJob(stubJobWithPcm(), 0f, 0f, 0f);
        assertDoesNotThrow(() -> {
            bridge.destroyEmitters(npc);
            bridge.destroyEmitters(npc);
        });
    }

    @Test
    void submitSecondJobDestroysFirstEmitter() {
        bridge.submitVoiceJob(stubJobWithPcm(), 0f, 0f, 0f);
        int countAfterFirst = bridge.activeEmitterCount();
        bridge.submitVoiceJob(stubJobWithPcm(), 1f, 0f, 0f);
        int countAfterSecond = bridge.activeEmitterCount();
        assertEquals(countAfterFirst, countAfterSecond);
    }

    @Test
    void updateEmitterPositionDoesNotThrowForUnknownNpc() {
        assertDoesNotThrow(() -> bridge.updateEmitterPosition(EntityId.of(999L), 1f, 2f, 3f));
    }

    @Test
    void isAvailableAlwaysTrueForDynamisAudioBridge() {
        assertTrue(bridge.isAvailable());
    }

    @Test
    void emptyPcmJobCreatesNoEmitter() {
        VoiceRenderJob emptyJob = VoiceRenderJob.stub(npc, "");
        bridge.submitVoiceJob(emptyJob, 0f, 0f, 0f);
        assertEquals(0, bridge.activeEmitterCount());
    }

    @Test
    void audioAssetSampleRateIs48kHz() {
        float[] samples = new float[2400];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        assertEquals(48000, asset.sampleRate());
    }

    @Test
    void audioAssetChannelCountIsMono() {
        float[] samples = new float[2400];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        assertEquals(1, asset.channelCount());
    }

    @Test
    void audioAssetResamplesFromLowerRate() {
        float[] samples = new float[24000];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        assertTrue(asset.totalFrames() > 40000 && asset.totalFrames() < 56000);
    }

    @Test
    void audioAssetReadFramesReturnsData() {
        float[] samples = new float[4800];
        java.util.Arrays.fill(samples, 0.5f);
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        float[] out = new float[512];
        int read = asset.readFrames(out, 512);
        assertTrue(read > 0);
    }

    @Test
    void audioAssetExhaustsAfterFullRead() {
        float[] samples = new float[480];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        float[] out = new float[8192];
        while (!asset.isExhausted()) {
            asset.readFrames(out, 1024);
        }
        assertEquals(0, asset.readFrames(out, 1024));
    }

    @Test
    void audioAssetResetAllowsReplay() {
        float[] samples = new float[4800];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 24000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        float[] out = new float[8192];
        while (!asset.isExhausted()) {
            asset.readFrames(out, 1024);
        }
        asset.reset();
        assertFalse(asset.isExhausted());
        assertTrue(asset.readFrames(out, 512) > 0);
    }

    @Test
    void audioAssetHandlesEmptyStream() {
        AudioStream empty = AudioStream.empty("test");
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(empty);
        assertEquals(48000, asset.sampleRate());
        assertTrue(asset.totalFrames() > 0);
    }

    @Test
    void audioAssetHandlesNativeRate() {
        float[] samples = new float[48000];
        AudioStream stream = AudioStream.fromPcmFloats("test", samples, 48000);
        VoiceRenderJobAudioAsset asset = new VoiceRenderJobAudioAsset(stream);
        assertEquals(48000, asset.totalFrames());
    }
}
