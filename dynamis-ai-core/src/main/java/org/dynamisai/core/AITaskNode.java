package org.dynamisai.core;

import java.util.Objects;

/**
 * A declared AI task with budget, priority, degrade policy, and fallback.
 * No task may be registered without a non-null fallback.
 */
public record AITaskNode(
    String taskId,
    int maxBudgetMs,
    Priority priority,
    DegradeMode degradeMode,
    Runnable task,
    Runnable fallback
) {
    public AITaskNode {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(fallback, "fallback must not be null — no task ships without one");
        if (maxBudgetMs <= 0) throw new IllegalArgumentException("maxBudgetMs must be > 0");
    }
}
