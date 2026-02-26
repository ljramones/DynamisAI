package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Default NavigationSystem — wires HPA* pathfinder + RVO2 steering.
 *
 * Pathfinding: virtual thread per request, results cached per agent.
 * Steering: synchronous — reads cached path, runs RVO2, returns SteeringOutput.
 * RVO2: solved across all registered agents each steer() call.
 */
public final class DefaultNavigationSystem implements NavigationSystem {

    private static final Logger log = LoggerFactory.getLogger(DefaultNavigationSystem.class);
    private static final int MAX_NODES = 512;

    private final NavMesh mesh;
    private final HpaGraph graph;
    private final HpaPathfinder pathfinder;
    private final RvoSolver rvoSolver;
    private final ExecutorService executor;

    private final Map<EntityId, NavPath> activePaths = new ConcurrentHashMap<>();
    private final Map<EntityId, RvoAgent> agentStates = new ConcurrentHashMap<>();

    public DefaultNavigationSystem(NavMesh mesh) {
        this.mesh = mesh;
        this.graph = HpaGraph.build(mesh);
        this.pathfinder = new DefaultHpaPathfinder();
        this.rvoSolver = new RvoSolver();
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("nav-", 0).factory());
        log.info("NavigationSystem ready — {} polys, {} clusters",
            mesh.polyCount(), mesh.clusterCount());
    }

    @Override
    public CompletableFuture<PathResult> requestPath(PathRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            NavPoint start = NavPoint.from(request.from());
            NavPoint goal = NavPoint.from(request.to());
            PathResult result = pathfinder.findPath(
                mesh, graph, start, goal, MAX_NODES, request.requester());

            if (result instanceof PathResult.Found found) {
                activePaths.put(request.requester(), found.path());
                log.debug("Path found for {} — {} waypoints, cost={}",
                    request.requester(),
                    found.path().waypoints().size(),
                    found.path().totalCost());
            } else {
                log.debug("Path {} for {}: {}",
                    result.getClass().getSimpleName(),
                    request.requester(),
                    result instanceof PathResult.Unreachable u ? u.reason() : "");
            }
            return result;
        }, executor);
    }

    @Override
    public SteeringOutput steer(EntityId agent, Location position, float speed) {
        NavPoint pos = NavPoint.from(position);
        NavPath path = activePaths.get(agent);

        if (path == null || !path.hasWaypoints()) {
            return SteeringOutput.idle(agent, pos);
        }

        NavPoint target = path.nextWaypoint();
        if (target == null) {
            activePaths.remove(agent);
            return SteeringOutput.idle(agent, pos);
        }

        float distToTarget = pos.distanceTo(target);

        if (distToTarget < 0.8f && path.waypoints().size() > 2) {
            NavPath advanced = new NavPath(
                path.waypoints().subList(1, path.waypoints().size()),
                path.totalCost() - distToTarget,
                path.isComplete());
            activePaths.put(agent, advanced);
            target = advanced.nextWaypoint();
            if (target == null) {
                return SteeringOutput.idle(agent, pos);
            }
            path = advanced;
        }

        float distToGoal = path.totalCost();

        RvoAgent rvoAgent = agentStates.get(agent);
        NavPoint steerVel;
        if (rvoAgent != null) {
            NavPoint preferred = pos.directionTo(target);
            preferred = NavPoint.of(preferred.x() * speed, 0, preferred.z() * speed);
            rvoAgent = rvoAgent.withPosition(pos).withPreferredVelocity(preferred);
            agentStates.put(agent, rvoAgent);

            var neighbourhood = new ArrayList<>(agentStates.values());
            var solved = rvoSolver.solve(neighbourhood, 0.05f);
            solved.stream().filter(a -> a.id().equals(agent)).findFirst()
                .ifPresent(a -> agentStates.put(agent, a));

            RvoAgent solvedAgent = agentStates.get(agent);
            steerVel = solvedAgent != null ? solvedAgent.velocity() : preferred;
        } else {
            NavPoint dir = pos.directionTo(target);
            steerVel = NavPoint.of(dir.x() * speed, 0, dir.z() * speed);
        }

        float actualSpeed = (float) Math.sqrt(
            steerVel.x() * steerVel.x() + steerVel.z() * steerVel.z());
        NavPoint lookDir = actualSpeed > 0.01f
            ? NavPoint.of(steerVel.x() / actualSpeed, 0, steerVel.z() / actualSpeed)
            : NavPoint.of(0, 0, 1);

        return new SteeringOutput(agent, steerVel, lookDir,
            actualSpeed, target, distToGoal,
            distToGoal < 0.5f);
    }

    @Override
    public void updateAgentState(RvoAgent agent) {
        agentStates.put(agent.id(), agent);
    }

    @Override
    public void removeAgent(EntityId agent) {
        activePaths.remove(agent);
        agentStates.remove(agent);
    }

    @Override
    public boolean isReady() {
        return mesh.polyCount() > 0;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
