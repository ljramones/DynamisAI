package org.dynamisai.core;

import java.util.List;

/**
 * Per-frame budget report — fed to AIInspector tooling.
 */
public record FrameBudgetReport(
    long tick,
    long frameStartNanos,
    long frameEndNanos,
    List<TaskExecutionRecord> taskRecords,
    int degradedTaskCount,
    int skippedTaskCount
) {
    public long frameElapsedMs() {
        return (frameEndNanos - frameStartNanos) / 1_000_000;
    }

    public static FrameBudgetReport empty(long tick) {
        long now = System.nanoTime();
        return new FrameBudgetReport(tick, now, now, List.of(), 0, 0);
    }
}
