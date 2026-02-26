package org.dynamisai.cognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses LLM JSON output into typed DialogueResponse.
 * Defensive — any malformed output produces a safe fallback, never an exception
 * that could propagate into game logic.
 */
public final class ResponseParser {

    private static final Logger log = LoggerFactory.getLogger(ResponseParser.class);

    public DialogueResponse parse(String json) {
        if (json == null || json.isBlank()) {
            log.warn("ResponseParser received empty input — returning fallback");
            return DialogueResponse.fallback("...");
        }
        try {
            String text = extractString(json, "text");
            AffectVector affect = extractAffect(json);
            List<String> tags = extractStringArray(json, "tags");
            List<BehaviorHint> hints = List.of();
            return new DialogueResponse(text, affect, tags, hints, false);
        } catch (Exception e) {
            log.warn("ResponseParser failed to parse LLM output — fallback. Input: {}",
                json.length() > 100 ? json.substring(0, 100) + "..." : json);
            return DialogueResponse.fallback("...");
        }
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "...";
        start += search.length();
        int end = json.indexOf('"', start);
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf('"', end + 1);
        }
        return end > start ? json.substring(start, end) : "...";
    }

    private AffectVector extractAffect(String json) {
        int affectStart = json.indexOf("\"affect\":");
        if (affectStart < 0) return AffectVector.neutral();
        int braceStart = json.indexOf('{', affectStart);
        int braceEnd = json.indexOf('}', braceStart);
        if (braceStart < 0 || braceEnd < 0) return AffectVector.neutral();
        String sub = json.substring(braceStart, braceEnd + 1);
        float valence = extractFloat(sub, "valence", 0f);
        float arousal = extractFloat(sub, "arousal", 0.3f);
        float dominance = extractFloat(sub, "dominance", 0.5f);
        float sarcasm = extractFloat(sub, "sarcasm", 0f);
        float intensity = extractFloat(sub, "intensity", 0.3f);
        return new AffectVector(
            clamp(valence, -1f, 1f),
            clamp(arousal, 0f, 1f),
            clamp(dominance, 0f, 1f),
            clamp(sarcasm, 0f, 1f),
            clamp(intensity, 0f, 1f)
        );
    }

    private float extractFloat(String json, String key, float defaultVal) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultVal;
        idx += search.length();
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
               || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Float.parseFloat(json.substring(idx, end));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private List<String> extractStringArray(String json, String key) {
        String search = "\"" + key + "\":[";
        int start = json.indexOf(search);
        if (start < 0) return List.of();
        start += search.length();
        int end = json.indexOf(']', start);
        if (end < 0) return List.of();
        String content = json.substring(start, end).trim();
        if (content.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (String part : content.split(",")) {
            String val = part.trim().replace("\"", "");
            if (!val.isEmpty()) result.add(val);
        }
        return List.copyOf(result);
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
