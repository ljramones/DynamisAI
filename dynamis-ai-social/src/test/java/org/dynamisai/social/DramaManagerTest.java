package org.dynamisai.social;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DramaManagerTest {

    @Test
    void freshManagerHasValidBeat() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        assertNotNull(dm.currentBeat());
    }

    @Test
    void highCombatTransitionsToEscalation() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.evaluate(new EngagementMetrics(0.9f, 0.1f, 0.5f, 0f, 0.5f, 300, 200), 200);
        assertEquals(DramaticBeat.ESCALATION, dm.currentBeat());
    }

    @Test
    void lowAfterEscalationTransitionsToRelief() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.forceBeat(DramaticBeat.ESCALATION, 0);
        dm.drainEvents();
        dm.evaluate(new EngagementMetrics(0.1f, 0.1f, 0.1f, 0f, 0.1f, 300, 200), 200);
        assertEquals(DramaticBeat.RELIEF, dm.currentBeat());
    }

    @Test
    void reliefEmitsResourceAvailability() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.forceBeat(DramaticBeat.ESCALATION, 0);
        dm.drainEvents();
        dm.evaluate(new EngagementMetrics(0.1f, 0.1f, 0.1f, 0f, 0.1f, 300, 200), 200);
        List<BehaviorModifierEvent> events = dm.drainEvents();
        assertTrue(events.stream().anyMatch(e -> e.type() == BehaviorModifierType.RESOURCE_AVAILABILITY));
    }

    @Test
    void escalationEmitsMaxAggression() {
        DramaManagerConfig c = new DramaManagerConfig(0.7f, 0.3f, 0.4f, 0.2f, 0, 0.77f);
        DramaManager dm = new DramaManager(c);
        dm.evaluate(new EngagementMetrics(0.9f, 0.1f, 0.5f, 0f, 0.5f, 300, 1), 1);
        List<BehaviorModifierEvent> events = dm.drainEvents();
        assertTrue(events.stream().anyMatch(e ->
            e.type() == BehaviorModifierType.FACTION_AGGRESSION
                && Math.abs(e.magnitude() - 0.77f) < 1e-6f));
    }

    @Test
    void minTicksBetweenChangesEnforced() {
        DramaManagerConfig c = new DramaManagerConfig(0.7f, 0.3f, 0.4f, 0.2f, 120, 0.8f);
        DramaManager dm = new DramaManager(c);
        dm.evaluate(new EngagementMetrics(0.9f, 0.1f, 0.5f, 0f, 0.5f, 300, 1), 1);
        assertEquals(DramaticBeat.ESCALATION, dm.currentBeat());
        dm.evaluate(new EngagementMetrics(0.1f, 0.1f, 0.1f, 0f, 0.1f, 300, 50), 50);
        assertEquals(DramaticBeat.ESCALATION, dm.currentBeat());
    }

    @Test
    void forceBeatOverridesImmediately() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.forceBeat(DramaticBeat.REVELATION, 1);
        assertEquals(DramaticBeat.REVELATION, dm.currentBeat());
    }

    @Test
    void drainClearsQueue() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.forceBeat(DramaticBeat.LULL, 1);
        assertFalse(dm.drainEvents().isEmpty());
        assertTrue(dm.drainEvents().isEmpty());
    }

    @Test
    void eventsOnlyOnTransitionNoDuplicates() {
        DramaManager dm = new DramaManager(new DramaManagerConfig(0.7f, 0.3f, 0.4f, 0.2f, 0, 0.8f));
        EngagementMetrics m = new EngagementMetrics(0.9f, 0.1f, 0.2f, 0f, 0.5f, 300, 1);
        dm.evaluate(m, 1);
        int firstCount = dm.drainEvents().size();
        dm.evaluate(m, 2);
        int secondCount = dm.drainEvents().size();
        assertTrue(firstCount > 0);
        assertEquals(0, secondCount);
    }

    @Test
    void evaluateNeverReturnsNull() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        assertNotNull(dm.evaluate(EngagementMetrics.neutral(1), 1));
    }

    @Test
    void dramaManagerCannotWriteWorldSnapshot() {
        DramaManager dm = new DramaManager(DramaManagerConfig.defaultConfig());
        dm.forceBeat(DramaticBeat.ESCALATION, 1);
        List<BehaviorModifierEvent> events = dm.drainEvents();
        assertTrue(events.stream().noneMatch(e -> {
            String p = String.valueOf(e.parameter()).toLowerCase();
            return p.contains("worldsnapshot") || p.contains("quest") || p.contains("narrative") || p.contains("canon");
        }));
    }
}
