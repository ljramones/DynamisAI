package org.dynamisai.demo;

import org.dynamisai.cognition.AffectVector;
import io.dynamis.audio.dsp.device.AudioDeviceException;
import org.dynamisai.tools.JavaSoundOutputNode;
import org.dynamisai.voice.AffectToVoiceStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowcaseTest {

    @Test
    void affectToVoiceStyleNeutral() {
        var style = AffectToVoiceStyle.from(AffectVector.neutral());
        assertEquals("[neutral]", style.kokokoToken());
        assertEquals(1.0f, style.pitchMult(), 0.01f);
    }

    @Test
    void affectToVoiceStyleHappy() {
        var style = AffectToVoiceStyle.from(new AffectVector(0.8f, 0.5f, 0.5f, 0f, 0.5f));
        assertEquals("[happy]", style.kokokoToken());
    }

    @Test
    void affectToVoiceStyleAngry() {
        var style = AffectToVoiceStyle.from(new AffectVector(-0.8f, 0.8f, 0.8f, 0f, 0.8f));
        assertEquals("[angry]", style.kokokoToken());
    }

    @Test
    void affectToVoiceStyleSad() {
        var style = AffectToVoiceStyle.from(new AffectVector(-0.6f, 0.1f, 0.5f, 0f, 0.4f));
        assertEquals("[sad]", style.kokokoToken());
    }

    @Test
    void affectToVoiceStyleHighArousalFasterRate() {
        var fast = AffectToVoiceStyle.from(new AffectVector(0f, 0.9f, 0.5f, 0f, 0.5f));
        var slow = AffectToVoiceStyle.from(new AffectVector(0f, 0.1f, 0.5f, 0f, 0.5f));
        assertTrue(fast.rateMult() > slow.rateMult());
    }

    @Test
    void affectToVoiceStyleHighDominanceLowerPitch() {
        var authoritative = AffectToVoiceStyle.from(new AffectVector(0f, 0.5f, 0.9f, 0f, 0.5f));
        var submissive = AffectToVoiceStyle.from(new AffectVector(0f, 0.5f, 0.1f, 0f, 0.5f));
        assertTrue(authoritative.pitchMult() < submissive.pitchMult());
    }

    @Test
    void applyToTextPrependsToken() {
        String result = AffectToVoiceStyle.applyToText(
            "Hello.", new AffectVector(0.8f, 0.5f, 0.5f, 0f, 0.5f));
        assertTrue(result.startsWith("[happy]"));
        assertTrue(result.contains("Hello."));
    }

    @Test
    void javaSoundOutputNodeIsClosedBeforeOpen() {
        JavaSoundOutputNode node = new JavaSoundOutputNode();
        assertFalse(node.isOpen());
    }

    @Test
    void javaSoundOutputNodeWriteBeforeOpenIsNoOp() {
        JavaSoundOutputNode node = new JavaSoundOutputNode();
        assertDoesNotThrow(() -> node.write(new float[512], 256, 2));
    }

    @Test
    void javaSoundOutputNodeCloseBeforeOpenIsNoOp() {
        JavaSoundOutputNode node = new JavaSoundOutputNode();
        assertDoesNotThrow(node::close);
    }

    @Test
    void javaSoundOutputNodeDoubleCloseIsIdempotent() throws Exception {
        JavaSoundOutputNode node = new JavaSoundOutputNode();
        try {
            node.open(48000, 2, 256);
            node.close();
            assertDoesNotThrow(node::close);
        } catch (AudioDeviceException e) {
            assumeTrue(false, "No audio hardware: " + e.getMessage());
        }
    }

    @Test
    void modelSetupMainHasEntryPoint() {
        assertDoesNotThrow(() -> {
            var m = ModelSetup.class.getMethod("main", String[].class);
            assertNotNull(m);
        });
    }
}
