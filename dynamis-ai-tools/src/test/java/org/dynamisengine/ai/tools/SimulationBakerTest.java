package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.core.DynamisAiEngine;
import org.dynamisengine.ai.core.HeadlessGameEngineAdapter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationBakerTest {

    @Test
    void bakeQuickScenarioProducesReport() {
        SimulationBaker baker = baker();
        BakeReport report = baker.bake(BakeScenario.quick("quick", 10));
        assertEquals(10, report.totalTicks());
    }

    @Test
    void alwaysFailingAssertionFailsBake() {
        SimulationBaker baker = baker();
        BakeScenario scenario = new BakeScenario("fail", 10, 1f / 60f,
            List.of(failing("alwaysFail")), false, 42L);
        BakeReport report = baker.bake(scenario);
        assertFalse(report.passed());
        assertFalse(report.violations().isEmpty());
    }

    @Test
    void alwaysPassingAssertionPassesBake() {
        SimulationBaker baker = baker();
        BakeScenario scenario = new BakeScenario("pass", 10, 1f / 60f,
            List.of(passing("alwaysPass")), false, 42L);
        BakeReport report = baker.bake(scenario);
        assertTrue(report.passed());
    }

    @Test
    void bakeAllReturnsReportsInOrder() {
        SimulationBaker baker = baker();
        List<BakeReport> reports = baker.bakeAll(List.of(
            BakeScenario.quick("one", 5),
            BakeScenario.quick("two", 7)
        ));
        assertEquals(2, reports.size());
        assertEquals(5, reports.getFirst().totalTicks());
        assertEquals(7, reports.get(1).totalTicks());
    }

    @Test
    void bakeWithProgressWritesOutput() {
        SimulationBaker baker = baker();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        baker.bakeWithProgress(BakeScenario.quick("progress", 10), 5, out);

        assertTrue(baos.toString().contains("[bake:progress]"));
    }

    @Test
    void ticksPerSecondIsPositiveAndTotalMatches() {
        SimulationBaker baker = baker();
        BakeReport report = baker.bake(BakeScenario.quick("speed", 20));
        assertTrue(report.ticksPerSecond() > 0);
        assertEquals(20, report.totalTicks());
    }

    @Test
    void sameSeedProducesSameViolationSequence() {
        BakeScenario scenario = new BakeScenario("seeded", 8, 1f / 60f,
            List.of(failing("alwaysFail")), false, 42L);

        BakeReport a = baker().bake(scenario);
        BakeReport b = baker().bake(scenario);

        assertEquals(a.violationCount(), b.violationCount());
        assertEquals(a.violations().stream().map(BakeViolation::tick).toList(),
            b.violations().stream().map(BakeViolation::tick).toList());
    }

    @Test
    void bakeWithNoEntitiesDoesNotThrow() {
        SimulationBaker baker = baker();
        BakeReport report = baker.bake(BakeScenario.quick("empty", 5));
        assertEquals(5, report.totalTicks());
    }

    private static SimulationBaker baker() {
        return new SimulationBaker(DynamisAiEngine.builder().build(), new HeadlessGameEngineAdapter());
    }

    private static BakeAssertion passing(String name) {
        return new BakeAssertion() {
            @Override public String name() { return name; }
            @Override public Optional<String> evaluate(long tick, org.dynamisengine.ai.core.AIOutputFrame frame) { return Optional.empty(); }
        };
    }

    private static BakeAssertion failing(String name) {
        return new BakeAssertion() {
            @Override public String name() { return name; }
            @Override public Optional<String> evaluate(long tick, org.dynamisengine.ai.core.AIOutputFrame frame) { return Optional.of("fail"); }
        };
    }
}
