package org.dynamisengine.ai.tools;

import java.util.List;

public record BakeScenario(
    String name,
    int tickCount,
    float deltaTimeSeconds,
    List<BakeAssertion> assertions,
    boolean attachInspector,
    long deterministicSeed
) {
    public BakeScenario {
        assertions = assertions == null ? List.of() : List.copyOf(assertions);
    }

    public static BakeScenario quick(String name, int tickCount) {
        return new BakeScenario(name, tickCount, 1f / 60f,
            List.of(
                CommonBakeAssertions.steeringOutputNotNull(),
                CommonBakeAssertions.noBlankDialogue(),
                CommonBakeAssertions.maxSpeed(50f)
            ),
            false, 42L);
    }

    public static BakeScenario full(String name) {
        return new BakeScenario("full-" + name, 10_000, 1f / 60f,
            List.of(
                CommonBakeAssertions.steeringOutputNotNull(),
                CommonBakeAssertions.noBlankDialogue(),
                CommonBakeAssertions.dialogueRateLimit(3),
                CommonBakeAssertions.maxSpeed(50f),
                CommonBakeAssertions.noMassStall(0.8f, 60)
            ),
            true, 42L);
    }
}
