package org.dynamisengine.ai.extensions;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.SteeringOutput;
import org.dynamisengine.ai.navigation.NavPath;
import org.dynamisengine.ai.navigation.NavPoint;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.navigation.PathRequest;
import org.dynamisengine.ai.navigation.PathResult;
import org.dynamisengine.ai.navigation.RvoAgent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal NavigationSystem SPI example.
 *
 * A real implementation would load a NavMesh, compute obstacle-aware paths,
 * and run local avoidance; this stub returns straight-line paths and steering.
 */
public final class ExampleNavigationSystem implements NavigationSystem {

    private final Map<EntityId, Location> goals = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<PathResult> requestPath(PathRequest request) {
        goals.put(request.requester(), request.to());
        NavPath path = new NavPath(
            List.of(NavPoint.from(request.from()), NavPoint.from(request.to())),
            distance(request.from(), request.to()),
            true);
        return CompletableFuture.completedFuture(new PathResult.Found(request.requester(), path));
    }

    @Override
    public SteeringOutput steer(EntityId agent, Location position, float speed) {
        Location goal = goals.get(agent);
        if (goal == null) {
            return SteeringOutput.stopped();
        }
        float dx = (float) (goal.x() - position.x());
        float dz = (float) (goal.z() - position.z());
        float mag = (float) Math.sqrt(dx * dx + dz * dz);
        if (mag < 1e-4f) {
            return SteeringOutput.atGoal();
        }
        Location dir = new Location(dx / mag, 0, dz / mag);
        return new SteeringOutput(dir, dir, 1f, false);
    }

    @Override
    public void updateAgentState(RvoAgent agent) {
        // no-op stub
    }

    @Override
    public void removeAgent(EntityId agent) {
        goals.remove(agent);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private static float distance(Location a, Location b) {
        float dx = (float) (a.x() - b.x());
        float dy = (float) (a.y() - b.y());
        float dz = (float) (a.z() - b.z());
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
