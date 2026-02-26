package org.dynamisai.voice;

/**
 * Configuration for a single TTS engine.
 * modelPath may be a directory (safetensors) or a single ONNX file.
 */
public record TtsModelConfig(
    String label,
    String modelPath,
    int sampleRateHz,
    int maxTokens
) {
    public TtsModelConfig {
        if (sampleRateHz <= 0)
            throw new IllegalArgumentException("sampleRateHz must be > 0");
        if (maxTokens <= 0)
            throw new IllegalArgumentException("maxTokens must be > 0");
    }

    public static TtsModelConfig chatterbox(String modelPath) {
        return new TtsModelConfig("chatterbox", modelPath, 24000, 1024);
    }

    public static TtsModelConfig bark(String modelPath) {
        return new TtsModelConfig("bark", modelPath, 24000, 256);
    }

    public static TtsModelConfig kokoro(String modelPath) {
        return new TtsModelConfig("kokoro", modelPath, 22050, 512);
    }
}
