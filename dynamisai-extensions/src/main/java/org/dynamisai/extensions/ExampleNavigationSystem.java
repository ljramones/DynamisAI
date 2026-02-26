package org.dynamisai.extensions;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.SteeringOutput;
import org.dynamisai.navigation.NavPath;
import org.dynamisai.navigation.NavPoint;
import org.dynamisai.navigation.NavigationSystem;
import org.dynamisai.navigation.PathRequest;
import org.dynamisai.navigation.PathResult;
import org.dynamisai.navigation.RvoAgent;

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
