package org.dynamisai.cognition;

/**
 * Configuration for a single LLM inference call.
 * Temperature 0.0 + deterministicMode = reproducible output for QA and replay.
 */
public record GenerationConfig(
    int maxTokens,
    float temperature,
    long seed,
    boolean deterministicMode
) {
    public GenerationConfig {
        if (maxTokens <= 0) throw new IllegalArgumentException("maxTokens must be > 0");
        if (temperature < 0f || temperature > 2f)
            throw new IllegalArgumentException("temperature must be 0.0-2.0");
    }

    /** Deterministic — same seed, same output. Used for QA, replay, baking. */
    public static GenerationConfig deterministic(long seed) {
        return new GenerationConfig(256, 0.0f, seed, true);
    }

    /** Creative — varied output for normal gameplay dialogue. */
    public static GenerationConfig creative(long seed) {
        return new GenerationConfig(512, 0.7f, seed, false);
    }

    /** Minimal — fast, short responses for low-priority cognitive tasks. */
    public static GenerationConfig minimal(long seed) {
        return new GenerationConfig(128, 0.5f, seed, false);
    }
}
