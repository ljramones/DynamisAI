package org.dynamisai.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBudgetGovernorTest {

    private DefaultBudgetGovernor governor;
    private WorldSnapshot stubSnapshot;

    @BeforeEach
    void setUp() {
        governor = new DefaultBudgetGovernor(8);
        stubSnapshot = new DefaultWorldStateStore().getCurrentSnapshot();
    }

    private AITaskNode alwaysRun(String id, Priority p, Runnable task) {
        return new AITaskNode(id, 1, p, DegradeMode.FULL, task, () -> {});
    }

    private AITaskNode withFallback(String id, Priority p, DegradeMode mode,
                                     Runnable task, Runnable fallback) {
        return new AITaskNode(id, 1, p, mode, task, fallback);
    }

    @Test
    void registeredTaskRuns() {
        AtomicBoolean ran = new AtomicBoolean(false);
        governor.register(alwaysRun("test", Priority.NORMAL, () -> ran.set(true)));
        governor.runFrame(1L, stubSnapshot);
        assertTrue(ran.get());
    }

    @Test
    void criticalTaskAlwaysRunsRegardlessOfBudget() {
        DefaultBudgetGovernor tightGovernor = new DefaultBudgetGovernor(1);
        AtomicBoolean criticalRan = new AtomicBoolean(false);
        AtomicBoolean normalRan = new AtomicBoolean(false);

        tightGovernor.register(new AITaskNode("slow", 1, Priority.NORMAL,
            DegradeMode.FULL,
            () -> { try { Thread.sleep(5); } catch (InterruptedException e) {} normalRan.set(true); },
            () -> {}));
        tightGovernor.register(new AITaskNode("critical", 1, Priority.CRITICAL,
            DegradeMode.FULL,
            () -> criticalRan.set(true),
            () -> {}));

        tightGovernor.runFrame(1L, stubSnapshot);
        assertTrue(criticalRan.get(), "CRITICAL task must always run");
    }

    @Test
    void taskRegistrationReplacesExistingById() {
        AtomicInteger count = new AtomicInteger(0);
        governor.register(alwaysRun("dup", Priority.NORMAL, count::incrementAndGet));
        governor.register(alwaysRun("dup", Priority.NORMAL, count::incrementAndGet));
        governor.runFrame(1L, stubSnapshot);
        assertEquals(1, count.get(), "Duplicate taskId should replace, not add");
    }

    @Test
    void unregisteredTaskDoesNotRun() {
        AtomicBoolean ran = new AtomicBoolean(false);
        governor.register(alwaysRun("removeme", Priority.NORMAL, () -> ran.set(true)));
        governor.unregister("removeme");
        governor.runFrame(1L, stubSnapshot);
        assertFalse(ran.get());
    }

    @Test
    void skipModeRunsNothingWhenBudgetExceeded() {
        DefaultBudgetGovernor tightGovernor = new DefaultBudgetGovernor(1);
        AtomicBoolean primaryRan = new AtomicBoolean(false);
        AtomicBoolean fallbackRan = new AtomicBoolean(false);

        tightGovernor.register(new AITaskNode("burner", 1, Priority.HIGH,
            DegradeMode.FULL,
            () -> { try { Thread.sleep(5); } catch (InterruptedException e) {} },
            () -> {}));
        tightGovernor.register(new AITaskNode("skipper", 1, Priority.NORMAL,
            DegradeMode.SKIP,
            () -> primaryRan.set(true),
            () -> fallbackRan.set(true)));

        tightGovernor.runFrame(1L, stubSnapshot);
        assertFalse(primaryRan.get());
        assertFalse(fallbackRan.get());
    }

    @Test
    void fallbackModeRunsFallbackWhenBudgetExceeded() {
        DefaultBudgetGovernor tightGovernor = new DefaultBudgetGovernor(1);
        AtomicBoolean primaryRan = new AtomicBoolean(false);
        AtomicBoolean fallbackRan = new AtomicBoolean(false);

        tightGovernor.register(new AITaskNode("burner", 1, Priority.HIGH,
            DegradeMode.FULL,
            () -> { try { Thread.sleep(5); } catch (InterruptedException e) {} },
            () -> {}));
        tightGovernor.register(new AITaskNode("withfallback", 1, Priority.NORMAL,
            DegradeMode.FALLBACK,
            () -> primaryRan.set(true),
            () -> fallbackRan.set(true)));

        tightGovernor.runFrame(1L, stubSnapshot);
        assertFalse(primaryRan.get());
        assertTrue(fallbackRan.get());
    }

    @Test
    void throwingTaskDoesNotAbortFrame() {
        AtomicBoolean secondRan = new AtomicBoolean(false);
        governor.register(alwaysRun("thrower", Priority.NORMAL,
            () -> { throw new RuntimeException("intentional"); }));
        governor.register(alwaysRun("second", Priority.NORMAL,
            () -> secondRan.set(true)));
        assertDoesNotThrow(() -> governor.runFrame(1L, stubSnapshot));
        assertTrue(secondRan.get());
    }

    @Test
    void frameReportRecordsAllTasks() {
        governor.register(alwaysRun("a", Priority.CRITICAL, () -> {}));
        governor.register(alwaysRun("b", Priority.NORMAL, () -> {}));
        governor.runFrame(1L, stubSnapshot);
        FrameBudgetReport report = governor.getLastFrameReport();
        assertNotNull(report);
        assertEquals(2, report.taskRecords().size());
    }

    @Test
    void tasksExecuteInPriorityOrder() {
        java.util.List<String> order = new java.util.ArrayList<>();
        governor.register(alwaysRun("low",      Priority.LOW,      () -> order.add("low")));
        governor.register(alwaysRun("normal",   Priority.NORMAL,   () -> order.add("normal")));
        governor.register(alwaysRun("critical", Priority.CRITICAL, () -> order.add("critical")));
        governor.register(alwaysRun("high",     Priority.HIGH,     () -> order.add("high")));
        governor.runFrame(1L, stubSnapshot);
        assertEquals(java.util.List.of("critical", "high", "normal", "low"), order);
    }

    @Test
    void nullFallbackIsRejected() {
        assertThrows(NullPointerException.class, () ->
            new AITaskNode("bad", 1, Priority.NORMAL, DegradeMode.FALLBACK,
                () -> {}, null));
    }

    @Test
    void nullTaskIsRejected() {
        assertThrows(NullPointerException.class, () ->
            new AITaskNode("bad", 1, Priority.NORMAL, DegradeMode.FULL,
                null, () -> {}));
    }

    @Test
    void zeroBudgetIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new AITaskNode("bad", 0, Priority.NORMAL, DegradeMode.FULL,
                () -> {}, () -> {}));
    }

    @Test
    void getRegisteredTaskCountIsAccurate() {
        assertEquals(0, governor.getRegisteredTaskCount());
        governor.register(alwaysRun("a", Priority.NORMAL, () -> {}));
        governor.register(alwaysRun("b", Priority.NORMAL, () -> {}));
        assertEquals(2, governor.getRegisteredTaskCount());
        governor.unregister("a");
        assertEquals(1, governor.getRegisteredTaskCount());
    }
}
