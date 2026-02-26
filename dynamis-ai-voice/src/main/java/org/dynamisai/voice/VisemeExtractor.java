package org.dynamisai.voice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rule-based grapheme-to-viseme extractor.
 *
 * Produces a lip-sync timeline from plain text without any ML model.
 */
public final class VisemeExtractor {

    /** Milliseconds per phoneme at normal speech rate. */
    private static final long PHONEME_DURATION_MS = 80L;

    /** Silence gap between words in ms. */
    private static final long WORD_GAP_MS = 60L;

    /** Grapheme clusters -> viseme label (longest match first). */
    private static final List<Map.Entry<String, String>> GRAPHEME_MAP = List.of(
        Map.entry("mb", "M"),
        Map.entry("mm", "M"),
        Map.entry("m", "M"),
        Map.entry("b", "M"),
        Map.entry("p", "M"),
        Map.entry("ff", "F"),
        Map.entry("ph", "F"),
        Map.entry("f", "F"),
        Map.entry("v", "F"),
        Map.entry("th", "TH"),
        Map.entry("dd", "DD"),
        Map.entry("tt", "DD"),
        Map.entry("t", "DD"),
        Map.entry("d", "DD"),
        Map.entry("ch", "CH"),
        Map.entry("sh", "CH"),
        Map.entry("zh", "CH"),
        Map.entry("j", "CH"),
        Map.entry("ss", "SS"),
        Map.entry("s", "SS"),
        Map.entry("z", "SS"),
        Map.entry("ng", "nn"),
        Map.entry("nn", "nn"),
        Map.entry("n", "nn"),
        Map.entry("l", "nn"),
        Map.entry("rr", "RR"),
        Map.entry("r", "RR"),
        Map.entry("kk", "kk"),
        Map.entry("ck", "kk"),
        Map.entry("k", "kk"),
        Map.entry("g", "kk"),
        Map.entry("q", "kk"),
        Map.entry("x", "kk"),
        Map.entry("ee", "EE"),
        Map.entry("ie", "EE"),
        Map.entry("ea", "EE"),
        Map.entry("ey", "EE"),
        Map.entry("i", "EE"),
        Map.entry("e", "EE"),
        Map.entry("oo", "OO"),
        Map.entry("ou", "OO"),
        Map.entry("ow", "OO"),
        Map.entry("u", "OO"),
        Map.entry("w", "OO"),
        Map.entry("oh", "OH"),
        Map.entry("oa", "OH"),
        Map.entry("o", "OH"),
        Map.entry("ah", "AH"),
        Map.entry("aa", "AH"),
        Map.entry("a", "AH")
    );

    /**
     * Extract viseme timeline from plain text.
     * Produces one VisemeTimestamp per detected phoneme.
     */
    public List<VisemeTimestamp> extract(String text) {
        if (text == null || text.isBlank()) return List.of();

        List<VisemeTimestamp> result = new ArrayList<>();
        String lower = text.toLowerCase().trim();
        long offsetMs = 0L;

        String[] words = lower.split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-z]", "");
            if (cleaned.isEmpty()) {
                offsetMs += WORD_GAP_MS;
                continue;
            }

            int pos = 0;
            while (pos < cleaned.length()) {
                String matched = null;
                String viseme = "AH";

                for (Map.Entry<String, String> entry : GRAPHEME_MAP) {
                    String grapheme = entry.getKey();
                    if (cleaned.startsWith(grapheme, pos)) {
                        matched = grapheme;
                        viseme = entry.getValue();
                        break;
                    }
                }

                if (matched == null) {
                    matched = String.valueOf(cleaned.charAt(pos));
                    viseme = "AH";
                }

                result.add(new VisemeTimestamp(
                    Duration.ofMillis(offsetMs), viseme, 0.85f));
                offsetMs += PHONEME_DURATION_MS;
                pos += matched.length();
            }
            offsetMs += WORD_GAP_MS;
        }

        return List.copyOf(result);
    }

    /**
     * Estimate total speech duration from text.
     * More accurate than character count — accounts for word gaps.
     */
    public Duration estimateDuration(String text) {
        if (text == null || text.isBlank()) return Duration.ofMillis(500L);
        List<VisemeTimestamp> visemes = extract(text);
        if (visemes.isEmpty()) return Duration.ofMillis(500L);
        VisemeTimestamp last = visemes.get(visemes.size() - 1);
        return last.offset().plusMillis(PHONEME_DURATION_MS + WORD_GAP_MS);
    }
}
