package org.dynamisengine.ai.planning;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.memory.MemoryStats;
import org.dynamisengine.ai.navigation.DefaultNavigationSystem;
import org.dynamisengine.ai.navigation.NavMeshBuilder;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.perception.PerceptionSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NavigationIntegrationTest {

    private NavigationSystem nav;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        nav = new DefaultNavigationSystem(
            NavMeshBuilder.buildGrid(8, 8, 2f, 4));
        npc = EntityId.of(42L);
    }

    @AfterEach
    void tearDown() {
        ((DefaultNavigationSystem) nav).shutdown();
    }

    @Test
    void fleeOperatorDoesNotThrow() {
        Runnable op = NavigationOperators.fleeFrom(nav, npc,
            new Location(1, 0, 1),
            new Location(5, 0, 5));
        assertDoesNotThrow(op::run);
    }

    @Test
    void moveToOperatorDoesNotThrow() {
        Runnable op = NavigationOperators.moveTo(nav, npc,
            new Location(10, 0, 10));
        assertDoesNotThrow(op::run);
    }

    @Test
    void stopOperatorDoesNotThrow() {
        assertDoesNotThrow(() ->
            NavigationOperators.stop(nav, npc).run());
    }

    @Test
    void taskLibraryFleeTaskWithNavCompiles() {
        HtnTask.PrimitiveTask task = TaskLibrary.fleeThreatTask(nav, npc,
            new Location(1, 0, 1), new Location(5, 0, 5));
        assertNotNull(task);
    }

    @Test
    void taskLibraryApproachTaskWithNavCompiles() {
        HtnTask.PrimitiveTask task = TaskLibrary.approachPlayerTask(nav, npc,
            new Location(10, 0, 10));
        assertNotNull(task);
    }

    @Test
    void taskLibraryIdleTaskWithNavCompiles() {
        HtnTask.PrimitiveTask task = TaskLibrary.idleTask(nav, npc);
        assertNotNull(task);
    }

    @Test
    void worldStateHoldsNavFields() {
        WorldState ws = WorldState.withoutNav(npc, 1L,
            AffectVector.neutral(),
            ThreatLevel.NONE,
            PerceptionSnapshot.empty(npc, 0L, new Location(0, 0, 0)),
            new MemoryStats(npc, 0, 0, 0, 0, 0),
            Map.of());
        assertEquals(0f, ws.distanceToGoal(), 0.001f);
        assertNotNull(ws.agentPosition());
        assertNotNull(ws.goalPosition());
    }

    @Test
    void worldStateNavFieldsRoundTrip() {
        org.dynamisengine.ai.navigation.NavPoint pos =
            org.dynamisengine.ai.navigation.NavPoint.of(3f, 0f, 7f);
        org.dynamisengine.ai.navigation.NavPoint goal =
            org.dynamisengine.ai.navigation.NavPoint.of(10f, 0f, 10f);
        float dist = pos.distanceTo(goal);

        WorldState ws = new WorldState(npc, 1L,
            AffectVector.neutral(),
            ThreatLevel.NONE,
            PerceptionSnapshot.empty(npc, 0L, new Location(0, 0, 0)),
            new MemoryStats(npc, 0, 0, 0, 0, 0),
            Map.of(),
            pos, goal, dist);

        assertEquals(3f, ws.agentPosition().x(), 0.001f);
        assertEquals(10f, ws.goalPosition().x(), 0.001f);
        assertEquals(dist, ws.distanceToGoal(), 0.001f);
    }
}
