package org.dynamisai.social;

public final class EngagementTracker {

    public static final int WINDOW_TICKS = 300;

    private final float[] combat = new float[WINDOW_TICKS];
    private final float[] exploration = new float[WINDOW_TICKS];
    private final float[] decision = new float[WINDOW_TICKS];
    private final float[] idle = new float[WINDOW_TICKS];
    private final float[] pressure = new float[WINDOW_TICKS];

    private int writeIndex = 0;
    private int size = 0;

    public void recordCombatTick(boolean inCombat) {
        combat[writeIndex] = inCombat ? 1f : 0f;
    }

    public void recordExploration(boolean newAreaVisited) {
        exploration[writeIndex] = newAreaVisited ? 1f : 0f;
    }

    public void recordDecisionSpeed(float speed) {
        float clamped = clamp01(speed);
        decision[writeIndex] = clamped;
        idle[writeIndex] = clamped < 0.05f ? 1f : 0f;
        advance();
    }

    public void recordThreatPressure(float p) {
        pressure[writeIndex] = clamp01(p);
    }

    public EngagementMetrics compute(long currentTick) {
        if (size == 0) {
            return EngagementMetrics.neutral(currentTick);
        }

        float combatAvg = average(combat, size);
        float explorationAvg = average(exploration, size);
        float decisionAvg = average(decision, size);
        float idleAvg = average(idle, size);
        float pressureAvg = average(pressure, size);

        return new EngagementMetrics(
            combatAvg,
            explorationAvg,
            decisionAvg,
            idleAvg,
            pressureAvg,
            size,
            currentTick
        );
    }

    private void advance() {
        writeIndex = (writeIndex + 1) % WINDOW_TICKS;
        if (size < WINDOW_TICKS) {
            size++;
        }
    }

    private static float average(float[] values, int n) {
        float sum = 0f;
        for (int i = 0; i < n; i++) {
            sum += values[i];
        }
        return n == 0 ? 0f : sum / n;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
