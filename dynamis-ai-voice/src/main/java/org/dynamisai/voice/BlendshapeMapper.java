package org.dynamisai.voice;

import org.dynamisai.cognition.AffectVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless mapper from visemes + affect to named blendshape frames.
 */
public final class BlendshapeMapper {

    private final BlendshapeTable table;

    public BlendshapeMapper(BlendshapeTable table) {
        this.table = table;
    }

    public List<BlendshapeFrame> map(List<VisemeTimestamp> visemes, AffectVector affect) {
        if (visemes == null || visemes.isEmpty()) {
            return List.of();
        }

        List<BlendshapeFrame> out = new ArrayList<>(visemes.size());
        for (VisemeTimestamp v : visemes) {
            out.add(new BlendshapeFrame(
                v.offset().toMillis() / 1000f,
                new BlendshapeWeights(computeWeights(v, affect))
            ));
        }
        return List.copyOf(out);
    }

    public BlendshapeFrame mapAtTime(List<VisemeTimestamp> visemes, AffectVector affect, float timeSeconds) {
        List<BlendshapeFrame> frames = map(visemes, affect);
        if (frames.isEmpty()) {
            return new BlendshapeFrame(timeSeconds, new BlendshapeWeights(Map.of()));
        }
        if (timeSeconds <= frames.get(0).timeSeconds()) {
            return frames.get(0);
        }
        if (timeSeconds >= frames.get(frames.size() - 1).timeSeconds()) {
            return frames.get(frames.size() - 1);
        }

        BlendshapeFrame left = frames.get(0);
        BlendshapeFrame right = frames.get(frames.size() - 1);
        for (int i = 1; i < frames.size(); i++) {
            if (frames.get(i).timeSeconds() >= timeSeconds) {
                left = frames.get(i - 1);
                right = frames.get(i);
                break;
            }
        }

        if (Math.abs(right.timeSeconds() - left.timeSeconds()) < 1e-6f) {
            return left;
        }

        float alpha = (timeSeconds - left.timeSeconds()) /
            (right.timeSeconds() - left.timeSeconds());
        Map<String, Float> blended = interpolate(left.weights().weights(), right.weights().weights(), alpha);
        return new BlendshapeFrame(timeSeconds, new BlendshapeWeights(blended));
    }

    private Map<String, Float> computeWeights(VisemeTimestamp viseme, AffectVector affect) {
        Map<String, Float> combined = new HashMap<>();
        Map<String, Float> base = table.visemeWeights().getOrDefault(viseme.viseme(),
            table.visemeWeights().getOrDefault("rest", Map.of()));

        for (Map.Entry<String, Float> e : base.entrySet()) {
            add(combined, e.getKey(), e.getValue() * viseme.weight());
        }

        float scale = affect.intensity();
        if (affect.valence() > 0f) {
            addScaled(combined, table.affectValencePos(), affect.valence() * scale);
        } else if (affect.valence() < 0f) {
            addScaled(combined, table.affectValenceNeg(), Math.abs(affect.valence()) * scale);
        }
        addScaled(combined, table.affectArousal(), affect.arousal() * scale);
        addScaled(combined, table.affectDominance(), affect.dominance() * scale);
        addScaled(combined, table.affectSarcasm(), affect.sarcasm() * scale);

        Map<String, Float> clamped = new LinkedHashMap<>();
        for (Map.Entry<String, Float> e : combined.entrySet()) {
            clamped.put(e.getKey(), clamp01(e.getValue()));
        }
        return clamped;
    }

    private static void addScaled(Map<String, Float> target, Map<String, Float> src, float scalar) {
        if (scalar <= 0f) {
            return;
        }
        for (Map.Entry<String, Float> e : src.entrySet()) {
            add(target, e.getKey(), e.getValue() * scalar);
        }
    }

    private static void add(Map<String, Float> target, String key, float value) {
        target.merge(key, value, Float::sum);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static Map<String, Float> interpolate(Map<String, Float> a, Map<String, Float> b, float alpha) {
        Map<String, Float> out = new HashMap<>();
        Set<String> keys = new java.util.HashSet<>();
        keys.addAll(a.keySet());
        keys.addAll(b.keySet());
        for (String k : keys) {
            float av = a.getOrDefault(k, 0f);
            float bv = b.getOrDefault(k, 0f);
            out.put(k, clamp01(av + (bv - av) * alpha));
        }
        return out;
    }
}
