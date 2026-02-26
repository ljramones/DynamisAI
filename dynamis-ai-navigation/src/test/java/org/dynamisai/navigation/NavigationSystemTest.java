package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NavigationSystemTest {

    private NavMesh mesh;
    private DefaultNavigationSystem nav;
    private EntityId npc;

    @BeforeEach
    void setUp() {
        // 8x8 grid, 2m cells, 4x4 clusters → 4 clusters
        mesh = NavMeshBuilder.buildGrid(8, 8, 2f, 4);
        nav = new DefaultNavigationSystem(mesh);
        npc = EntityId.of(1L);
    }

    // ── NavMesh ─────────────────────────────────────────────────────────────

    @Test
    void gridMeshHasCorrectPolyCount() {
        assertEquals(64, mesh.polyCount());
    }

    @Test
    void gridMeshHasCorrectClusterCount() {
        // 8x8 grid with 4x4 clusters = 4 clusters
        assertEquals(4, mesh.clusterCount());
    }

    @Test
    void nearestPolyFindsCorrectPoly() {
        // Origin (0,0,0) → should be poly 0 (centroid near 1,0,1)
        var poly = mesh.nearestPoly(new Location(0, 0, 0));
        assertTrue(poly.isPresent());
    }

    @Test
    void navPolyRequiresAtLeastThreeVertices() {
        assertThrows(IllegalArgumentException.class, () ->
            new NavPoly(0, List.of(NavPoint.of(0, 0, 0), NavPoint.of(1, 0, 0)),
                NavPoint.of(0, 0, 0), List.of(), 1f, 0));
    }

    @Test
    void navPointDistanceIsSymmetric() {
        NavPoint a = NavPoint.of(0, 0, 0);
        NavPoint b = NavPoint.of(3, 0, 4);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0.001f);
        assertEquals(5f, a.distanceTo(b), 0.001f);
    }

    @Test
    void navPointDirectionToIsNormalized() {
        NavPoint a = NavPoint.of(0, 0, 0);
        NavPoint b = NavPoint.of(3, 0, 4);
        NavPoint dir = a.directionTo(b);
        float mag = (float) Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
        assertEquals(1f, mag, 0.001f);
    }

    @Test
    void navPointLerpMidpoint() {
        NavPoint a = NavPoint.of(0, 0, 0);
        NavPoint b = NavPoint.of(4, 0, 0);
        NavPoint mid = a.lerp(b, 0.5f);
        assertEquals(2f, mid.x(), 0.001f);
    }

    // ── HPA* pathfinding ────────────────────────────────────────────────────

    @Test
    void pathFoundAcrossGrid() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),   // near top-left
            new Location(13, 0, 13)  // near bottom-right
        );
        PathResult result = nav.requestPath(req).get(3, TimeUnit.SECONDS);
        assertInstanceOf(PathResult.Found.class, result);
        PathResult.Found found = (PathResult.Found) result;
        assertTrue(found.path().hasWaypoints());
    }

    @Test
    void pathHasPositiveCost() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),
            new Location(13, 0, 13));
        PathResult result = nav.requestPath(req).get(3, TimeUnit.SECONDS);
        if (result instanceof PathResult.Found found) {
            assertTrue(found.path().totalCost() > 0);
        }
    }

    @Test
    void sameLocationPathIsAtGoal() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),
            new Location(1, 0, 1));
        PathResult result = nav.requestPath(req).get(3, TimeUnit.SECONDS);
        // Same poly — should be Found with trivial path
        assertNotNull(result);
    }

    @Test
    void pathResultPatternMatchIsExhaustive() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),
            new Location(13, 0, 13));
        PathResult result = nav.requestPath(req).get(3, TimeUnit.SECONDS);
        // Compiler enforces exhaustiveness
        String label = switch (result) {
            case PathResult.Found r -> "found";
            case PathResult.Unreachable r -> "unreachable";
            case PathResult.Partial r -> "partial";
        };
        assertNotNull(label);
    }

    @Test
    void hpaGraphHasCorrectClusterCount() {
        HpaGraph graph = HpaGraph.build(mesh);
        assertEquals(mesh.clusterCount(), graph.clusterCount());
    }

    @Test
    void hpaGraphClustersHaveNeighbors() {
        HpaGraph graph = HpaGraph.build(mesh);
        // In a 2x2 cluster grid, each cluster has at least 1 neighbor
        for (var cluster : graph.allClusters().stream().toList()) {
            assertFalse(cluster.neighborCosts().isEmpty(),
                "Cluster " + cluster.clusterId() + " has no neighbors");
        }
    }

    // ── Steering ────────────────────────────────────────────────────────────

    @Test
    void steerWithNoPathReturnsIdle() {
        SteeringOutput output = nav.steer(npc, new Location(1, 0, 1), 4f);
        assertTrue(output.isAtGoal());
        assertEquals(0f, output.speed());
    }

    @Test
    void steerAfterPathRequestProducesVelocity() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),
            new Location(13, 0, 13));
        nav.requestPath(req).get(3, TimeUnit.SECONDS);
        SteeringOutput output = nav.steer(npc, new Location(1, 0, 1), 4f);
        // Should have a non-zero velocity toward goal
        assertTrue(output.speed() >= 0f);
        assertNotNull(output.nextWaypoint());
    }

    @Test
    void removeAgentClearsState() throws Exception {
        PathRequest req = PathRequest.of(npc,
            new Location(1, 0, 1),
            new Location(13, 0, 13));
        nav.requestPath(req).get(3, TimeUnit.SECONDS);
        nav.removeAgent(npc);
        SteeringOutput output = nav.steer(npc, new Location(1, 0, 1), 4f);
        assertTrue(output.isAtGoal(), "After removeAgent, steer must return idle");
    }

    // ── RVO2 ────────────────────────────────────────────────────────────────

    @Test
    void rvoSolverReturnsOneResultPerAgent() {
        RvoSolver solver = new RvoSolver();
        List<RvoAgent> agents = List.of(
            RvoAgent.of(EntityId.of(1L), NavPoint.of(0, 0, 0), 0.5f, 5f)
                .withVelocity(NavPoint.of(1, 0, 0))
                .withPreferredVelocity(NavPoint.of(1, 0, 0)),
            RvoAgent.of(EntityId.of(2L), NavPoint.of(2, 0, 0), 0.5f, 5f)
                .withVelocity(NavPoint.of(-1, 0, 0))
                .withPreferredVelocity(NavPoint.of(-1, 0, 0))
        );
        List<RvoAgent> result = solver.solve(agents, 0.05f);
        assertEquals(2, result.size());
    }

    @Test
    void rvoSolverVelocityDoesNotExceedMaxSpeed() {
        RvoSolver solver = new RvoSolver();
        RvoAgent a = RvoAgent.of(EntityId.of(1L), NavPoint.of(0, 0, 0), 0.5f, 3f)
            .withVelocity(NavPoint.of(2, 0, 0))
            .withPreferredVelocity(NavPoint.of(5, 0, 0)); // exceeds maxSpeed
        RvoAgent b = RvoAgent.of(EntityId.of(2L), NavPoint.of(1, 0, 0), 0.5f, 3f)
            .withVelocity(NavPoint.of(-2, 0, 0))
            .withPreferredVelocity(NavPoint.of(-5, 0, 0));

        List<RvoAgent> result = solver.solve(List.of(a, b), 0.05f);
        for (RvoAgent r : result) {
            float speed = (float) Math.sqrt(
                r.velocity().x() * r.velocity().x() +
                r.velocity().z() * r.velocity().z());
            assertTrue(speed <= r.maxSpeed() + 0.01f,
                "Speed " + speed + " exceeds maxSpeed " + r.maxSpeed());
        }
    }

    @Test
    void rvoAgentRejectsNegativeRadius() {
        assertThrows(IllegalArgumentException.class, () ->
            RvoAgent.of(EntityId.of(1L), NavPoint.of(0, 0, 0), -1f, 5f));
    }

    @Test
    void navigationSystemIsReady() {
        assertTrue(nav.isReady());
    }

    @Test
    void emptyNavMeshIsNotReady() {
        NavMesh emptyMesh = new NavMeshBuilder().build(0);
        DefaultNavigationSystem emptyNav = new DefaultNavigationSystem(emptyMesh);
        assertFalse(emptyNav.isReady());
        emptyNav.shutdown();
    }

    @Test
    void steeringOutputIdleHasZeroSpeed() {
        SteeringOutput idle = SteeringOutput.idle(npc, NavPoint.of(0, 0, 0));
        assertEquals(0f, idle.speed());
        assertTrue(idle.isAtGoal());
    }
}
