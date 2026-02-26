package org.dynamisai.voice;

/**
 * Represents a rendered audio stream.
 * Stub — full PCM/buffer implementation arrives with DJL/Chatterbox wiring.
 */
public record AudioStream(
    String label,
    byte[] pcmData,
    int sampleRateHz,
    int channelCount,
    java.time.Duration estimatedDuration
) {
    /** Empty stub stream for use before DJL pipeline is wired. */
    public static AudioStream empty(String label) {
        return new AudioStream(label, new byte[0], 22050, 1,
            java.time.Duration.ZERO);
    }

    /**
     * Convert float[] PCM samples (range -1.0 to 1.0) to 16-bit signed little-endian PCM.
     * Used by DJL vocoder output conversion.
     */
    public static AudioStream fromPcmFloats(String label, float[] samples, int sampleRateHz) {
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            short s = (short) (Math.max(-1f, Math.min(1f, samples[i])) * Short.MAX_VALUE);
            bytes[i * 2] = (byte) (s & 0xFF);
            bytes[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        long durationMs = sampleRateHz > 0 ? 1000L * samples.length / sampleRateHz : 0L;
        return new AudioStream(label, bytes, sampleRateHz, 1,
            java.time.Duration.ofMillis(durationMs));
    }

    /** Convert byte[] back to float[] for downstream processing. */
    public float[] toPcmFloats() {
        float[] result = new float[pcmData.length / 2];
        for (int i = 0; i < result.length; i++) {
            short s = (short) ((pcmData[i * 2] & 0xFF) | (pcmData[i * 2 + 1] << 8));
            result[i] = s / (float) Short.MAX_VALUE;
        }
        return result;
    }
}
