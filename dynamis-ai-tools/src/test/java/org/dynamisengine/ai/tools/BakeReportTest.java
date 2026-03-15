package org.dynamisengine.ai.tools;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BakeReportTest {

    @Test
    void passedTrueWhenNoViolations() {
        BakeReport report = new BakeReport(1, 10, 10, 0, List.of(), 100, 1000, Map.of());
        assertTrue(report.passed());
        assertTrue(report.summary().contains("PASS"));
    }

    @Test
    void passedFalseWhenViolationsPresent() {
        BakeReport report = new BakeReport(1, 10, 10, 1,
            List.of(new BakeViolation("a", 1, "m", "f")),
            100, 1000, Map.of("a", 1));
        assertFalse(report.passed());
        assertTrue(report.summary().contains("FAIL"));
    }

    @Test
    void summaryContainsTickRangeAndViolationCount() {
        BakeReport report = new BakeReport(5, 15, 11, 3, List.of(), 200, 55, Map.of());
        String summary = report.summary();
        assertTrue(summary.contains("[5-15]"));
        assertTrue(summary.contains("3 violations"));
    }
}
