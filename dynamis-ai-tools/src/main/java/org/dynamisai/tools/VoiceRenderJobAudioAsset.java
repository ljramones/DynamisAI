package org.dynamisai.tools;

import io.dynamis.audio.api.AudioAsset;
import org.dynamisai.voice.AudioStream;

/**
 * Wraps VoiceRenderJob AudioStream as a DynamisAudio AudioAsset.
 * Always exposes mono 48kHz float frames.
 */
public final class VoiceRenderJobAudioAsset implements AudioAsset {

    private static final int TARGET_RATE = 48_000;
    private static final int CHANNELS = 1;

    private final float[] frames;
    private int cursor;

    public VoiceRenderJobAudioAsset(AudioStream stream) {
        this.frames = to48kMono(stream);
        this.cursor = 0;
    }

    @Override
    public int sampleRate() {
        return TARGET_RATE;
    }

    @Override
    public int channelCount() {
        return CHANNELS;
    }

    @Override
    public long totalFrames() {
        return frames.length;
    }

    @Override
    public int readFrames(float[] out, int frameCount) {
        if (isExhausted() || frameCount <= 0 || out == null || out.length == 0) {
            return 0;
        }

        int toRead = Math.min(frameCount, frames.length - cursor);
        toRead = Math.min(toRead, out.length);
        System.arraycopy(frames, cursor, out, 0, toRead);
        cursor += toRead;
        return toRead;
    }

    @Override
    public void reset() {
        cursor = 0;
    }

    @Override
    public boolean isExhausted() {
        return cursor >= frames.length;
    }

    private static float[] to48kMono(AudioStream stream) {
        if (stream == null) {
            return new float[]{0f};
        }

        float[] in = decodePcm16Le(stream.pcmData());
        if (in.length == 0) {
            // Non-zero silent fallback to keep downstream allocators happy.
            return new float[480];
        }

        int sourceRate = stream.sampleRateHz() > 0 ? stream.sampleRateHz() : TARGET_RATE;
        if (sourceRate == TARGET_RATE) {
            return in.clone();
        }

        int outLen = Math.max(1, (int) Math.round(in.length * (TARGET_RATE / (double) sourceRate)));
        float[] out = new float[outLen];

        double step = sourceRate / (double) TARGET_RATE;
        for (int i = 0; i < outLen; i++) {
            double srcPos = i * step;
            int idx = (int) Math.floor(srcPos);
            int next = Math.min(idx + 1, in.length - 1);
            double frac = srcPos - idx;
            out[i] = (float) ((in[idx] * (1.0 - frac)) + (in[next] * frac));
        }

        return out;
    }

    private static float[] decodePcm16Le(byte[] pcmData) {
        if (pcmData == null || pcmData.length < 2) {
            return new float[0];
        }
        int sampleCount = pcmData.length / 2;
        float[] out = new float[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            int lo = pcmData[i * 2] & 0xFF;
            int hi = pcmData[i * 2 + 1];
            short sample = (short) (lo | (hi << 8));
            out[i] = sample / (float) Short.MAX_VALUE;
        }
        return out;
    }
}
