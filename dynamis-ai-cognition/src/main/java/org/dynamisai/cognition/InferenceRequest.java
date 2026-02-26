package org.dynamisai.cognition;

/**
 * Inference input envelope carrying dialogue payload and optional deterministic seed.
 */
public record InferenceRequest(
    DialogueRequest dialogue,
    long deterministicSeed,
    boolean seedingEnabled
) {
    public static InferenceRequest unseeded(DialogueRequest dialogue) {
        return new InferenceRequest(dialogue, 0L, false);
    }

    public static InferenceRequest seeded(DialogueRequest dialogue, long seed) {
        return new InferenceRequest(dialogue, seed, true);
    }
}

