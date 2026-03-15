package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.core.AIOutputFrame;
import org.dynamisengine.ai.core.DynamisAiEngine;
import org.dynamisengine.ai.core.GameEngineContext;
import org.dynamisengine.ai.core.HeadlessGameEngineAdapter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SimulationBaker {

    private final DynamisAiEngine engine;
    private final HeadlessGameEngineAdapter adapter;

    public SimulationBaker(DynamisAiEngine engine, HeadlessGameEngineAdapter adapter) {
        this.engine = engine;
        this.adapter = adapter;
        this.adapter.initialize(engine);
    }

    public BakeReport bake(BakeScenario scenario) {
        return run(scenario, 0, null);
    }

    public List<BakeReport> bakeAll(List<BakeScenario> scenarios) {
        List<BakeReport> reports = new ArrayList<>();
        for (BakeScenario scenario : scenarios) {
            reports.add(bake(scenario));
        }
        return List.copyOf(reports);
    }

    public BakeReport bakeWithProgress(BakeScenario scenario, int progressIntervalTicks, PrintStream out) {
        return run(scenario, progressIntervalTicks, out == null ? System.out : out);
    }

    private BakeReport run(BakeScenario scenario, int progressIntervalTicks, PrintStream out) {
        long wallStart = System.currentTimeMillis();
        long startTick = scenario.deterministicSeed() != 0L
            ? Math.max(1L, scenario.deterministicSeed())
            : engine.currentTick() + 1L;

        AIInspector inspector = null;
        if (scenario.attachInspector()) {
            inspector = new AIInspector();
            engine.attachInspector(inspector);
        }

        List<BakeViolation> violations = new ArrayList<>();
        Map<String, Integer> byAssertion = new LinkedHashMap<>();

        for (int i = 0; i < scenario.tickCount(); i++) {
            long tick = startTick + i;
            GameEngineContext ctx = GameEngineContext.builder(tick, scenario.deltaTimeSeconds()).build();
            AIOutputFrame frame = adapter.tick(ctx);

            for (BakeAssertion assertion : scenario.assertions()) {
                Optional<String> failure = assertion.evaluate(tick, frame);
                if (failure.isPresent()) {
                    BakeViolation violation = new BakeViolation(
                        assertion.name(),
                        tick,
                        failure.get(),
                        summarize(frame)
                    );
                    violations.add(violation);
                    byAssertion.put(assertion.name(), byAssertion.getOrDefault(assertion.name(), 0) + 1);
                }
            }

            if (out != null && progressIntervalTicks > 0 && ((i + 1) % progressIntervalTicks == 0)) {
                long elapsed = Math.max(1L, System.currentTimeMillis() - wallStart);
                double tps = (i + 1) * 1000.0 / elapsed;
                out.printf("[bake:%s] tick %d/%d violations=%d tps=%.0f%n",
                    scenario.name(), i + 1, scenario.tickCount(), violations.size(), tps);
            }
        }

        long wallMs = Math.max(1L, System.currentTimeMillis() - wallStart);
        if (inspector != null) {
            engine.detachInspector();
        }

        double ticksPerSecond = scenario.tickCount() * 1000.0 / wallMs;
        long endTick = scenario.tickCount() == 0 ? startTick : startTick + scenario.tickCount() - 1L;

        return new BakeReport(
            startTick,
            endTick,
            scenario.tickCount(),
            violations.size(),
            violations,
            wallMs,
            ticksPerSecond,
            byAssertion
        );
    }

    private static String summarize(AIOutputFrame frame) {
        if (frame == null) {
            return "frame=null";
        }
        int entities = frame.worldSnapshot() == null ? 0 : frame.worldSnapshot().entities().size();
        int steering = frame.steeringOutputs() == null ? 0 : frame.steeringOutputs().size();
        int dialogue = frame.dialogueEvents() == null ? 0 : frame.dialogueEvents().size();
        return "entities=" + entities + " steering=" + steering + " dialogue=" + dialogue;
    }
}
