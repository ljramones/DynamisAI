package org.dynamisai.voice;

/**
 * Simple character-level tokenizer stub.
 * Production replacement: SentencePiece or BPE tokenizer loaded alongside model weights.
 * Stub produces deterministic int[] from character codepoints — sufficient for
 * ONNX input shape validation and test without live model.
 */
public final class TextTokenizer {

    private static final int VOCAB_SIZE = 32000;
    private static final int EOS_TOKEN = 1;

    /** Tokenize text to int[] — stub uses character codepoints mod VOCAB_SIZE. */
    public int[] tokenize(String text) {
        if (text == null || text.isBlank()) return new int[]{EOS_TOKEN};
        int[] tokens = new int[text.length() + 1];
        for (int i = 0; i < text.length(); i++) {
            tokens[i] = (text.charAt(i) % (VOCAB_SIZE - 2)) + 2;
        }
        tokens[text.length()] = EOS_TOKEN;
        return tokens;
    }

    /** Pad or truncate tokens to a fixed length for model input. */
    public int[] tokenizeFixed(String text, int length) {
        int[] raw = tokenize(text);
        int[] fixed = new int[length];
        System.arraycopy(raw, 0, fixed, 0, Math.min(raw.length, length));
        return fixed;
    }
}
