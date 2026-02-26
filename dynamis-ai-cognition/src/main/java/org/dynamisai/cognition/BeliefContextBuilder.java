package org.dynamisai.cognition;

import java.util.Comparator;
import java.util.stream.Collectors;

public final class BeliefContextBuilder {

    private BeliefContextBuilder() {}

    public static String buildContext(BeliefModel model, int maxBeliefs) {
        if (maxBeliefs <= 0) {
            return "";
        }

        var beliefs = model.allBeliefs().stream()
            .sorted(Comparator.comparing(Belief::confidence).reversed())
            .limit(maxBeliefs)
            .toList();

        if (beliefs.isEmpty()) {
            return "Known facts (NPC beliefs, confidence in brackets):";
        }

        String lines = beliefs.stream()
            .map(b -> "- " + b.key() + ": " + String.valueOf(b.value()) +
                " [" + String.format("%.2f", b.confidence()) + "]")
            .collect(Collectors.joining("\n"));

        return "Known facts (NPC beliefs, confidence in brackets):\n" + lines;
    }
}
