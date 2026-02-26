package org.dynamisai.core;

/**
 * Execution record for a single task in a frame — feeds time-travel debugger.
 */
public record TaskExecutionRecord(
    String taskId,
    Priority priority,
    QosLevel qosApplied,
    long elapsedNanos,
    boolean wasDegraded,
    boolean wasSkipped
) {}
