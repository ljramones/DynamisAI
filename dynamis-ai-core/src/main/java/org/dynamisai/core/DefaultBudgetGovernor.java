package org.dynamisai.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultBudgetGovernor implements BudgetGovernor {

    private static final Logger log = LoggerFactory.getLogger(DefaultBudgetGovernor.class);

    /** Total AI budget per frame in milliseconds. Default 8ms (leaves room for render). */
    private final int frameBudgetMs;

    private final CopyOnWriteArrayList<AITaskNode> tasks = new CopyOnWriteArrayList<>();
    private volatile FrameBudgetReport lastReport;

    public DefaultBudgetGovernor(int frameBudgetMs) {
        this.frameBudgetMs = frameBudgetMs;
    }

    public DefaultBudgetGovernor() {
        this(8);
    }

    @Override
    public void register(AITaskNode node) {
        tasks.removeIf(t -> t.taskId().equals(node.taskId()));
        tasks.add(node);
        tasks.sort(Comparator.comparingInt(t -> t.priority().ordinal()));
    }

    @Override
    public void unregister(String taskId) {
        tasks.removeIf(t -> t.taskId().equals(taskId));
    }

    @Override
    public void runFrame(long tick, WorldSnapshot snapshot) {
        long frameStart = System.nanoTime();
        List<TaskExecutionRecord> records = new ArrayList<>();
        int degraded = 0;
        int skipped = 0;

        for (AITaskNode node : tasks) {
            long taskStart = System.nanoTime();
            long elapsedMsSoFar = (taskStart - frameStart) / 1_000_000;

            boolean isCritical = node.priority() == Priority.CRITICAL;
            boolean budgetExceeded = (elapsedMsSoFar + node.maxBudgetMs()) > frameBudgetMs;

            if (isCritical || !budgetExceeded || node.degradeMode() == DegradeMode.FULL) {
                runSafely(node.taskId(), node.task());
                long elapsed = System.nanoTime() - taskStart;
                records.add(new TaskExecutionRecord(
                    node.taskId(), node.priority(), QosLevel.FULL, elapsed, false, false));
            } else {
                switch (node.degradeMode()) {
                    case SKIP -> {
                        log.debug("SKIP {} — budget exceeded", node.taskId());
                        long elapsed = System.nanoTime() - taskStart;
                        records.add(new TaskExecutionRecord(
                            node.taskId(), node.priority(), QosLevel.SKIP, elapsed, true, true));
                        skipped++;
                    }
                    case CACHED, DEFER, FALLBACK -> {
                        log.debug("{} {} — budget exceeded, running fallback", node.degradeMode(), node.taskId());
                        runSafely(node.taskId() + "[fallback]", node.fallback());
                        long elapsed = System.nanoTime() - taskStart;
                        QosLevel qos = switch (node.degradeMode()) {
                            case CACHED -> QosLevel.CACHED;
                            case DEFER -> QosLevel.DEFER;
                            default -> QosLevel.FALLBACK;
                        };
                        records.add(new TaskExecutionRecord(
                            node.taskId(), node.priority(), qos, elapsed, true, false));
                        degraded++;
                    }
                    default -> {
                        runSafely(node.taskId(), node.task());
                        long elapsed = System.nanoTime() - taskStart;
                        records.add(new TaskExecutionRecord(
                            node.taskId(), node.priority(), QosLevel.FULL, elapsed, false, false));
                    }
                }
            }
        }

        long frameEnd = System.nanoTime();
        lastReport = new FrameBudgetReport(
            tick, frameStart, frameEnd,
            Collections.unmodifiableList(records),
            degraded, skipped
        );
    }

    @Override
    public FrameBudgetReport getLastFrameReport() {
        return lastReport;
    }

    @Override
    public int getRegisteredTaskCount() {
        return tasks.size();
    }

    private void runSafely(String label, Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            log.error("Task '{}' threw an exception — continuing frame", label, e);
        }
    }
}
