package org.dynamisengine.ai.core;

import org.dynamisengine.core.entity.EntityId;
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
    Runnable fallback,
    EntityId entityId
) {
    public AITaskNode(String taskId,
                      int maxBudgetMs,
                      Priority priority,
                      DegradeMode degradeMode,
                      Runnable task,
                      Runnable fallback) {
        this(taskId, maxBudgetMs, priority, degradeMode, task, fallback, null);
    }

    public AITaskNode {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(fallback, "fallback must not be null — no task ships without one");
        if (maxBudgetMs <= 0) throw new IllegalArgumentException("maxBudgetMs must be > 0");
    }
}
