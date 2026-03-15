package org.dynamisengine.ai.tools;

import java.util.List;
import java.util.Map;

public record BakeReport(
    long startTick,
    long endTick,
    int totalTicks,
    int violationCount,
    List<BakeViolation> violations,
    long wallTimeMs,
    double ticksPerSecond,
    Map<String, Integer> violationsByAssertion
) {
    public BakeReport {
        violations = violations == null ? List.of() : List.copyOf(violations);
        violationsByAssertion = violationsByAssertion == null ? Map.of() : Map.copyOf(violationsByAssertion);
    }

    public boolean passed() {
        return violationCount == 0;
    }

    public String summary() {
        return String.format(
            "Bake [%d-%d] %d ticks in %dms (%.0f ticks/s) — %s (%d violations)",
            startTick, endTick, totalTicks, wallTimeMs,
            ticksPerSecond, passed() ? "PASS" : "FAIL", violationCount);
    }
}
