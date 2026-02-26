package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Two-level HPA* implementation.
 *
 * Phase 1: A* on HpaGraph to find cluster sequence.
 * Phase 2: A* on NavMesh polys to refine waypoints within each cluster segment.
 * Output: ordered NavPoint waypoints (poly centroids) from start to goal.
 *
 * Thread-safe: stateless — all state is on the stack.
 */
public final class DefaultHpaPathfinder implements HpaPathfinder {

    private static final Logger log = LoggerFactory.getLogger(DefaultHpaPathfinder.class);

    @Override
    public PathResult findPath(NavMesh mesh, HpaGraph graph,
                               NavPoint start, NavPoint goal,
                               int maxNodes, EntityId requester) {
        Optional<NavPoly> startPoly = mesh.nearestPoly(start);
        Optional<NavPoly> goalPoly = mesh.nearestPoly(goal);

        if (startPoly.isEmpty() || goalPoly.isEmpty()) {
            return new PathResult.Unreachable(requester, "Start or goal not on NavMesh");
        }

        int startCluster = startPoly.get().clusterId();
        int goalCluster = goalPoly.get().clusterId();

        List<Integer> clusterSequence;
        if (startCluster == goalCluster) {
            clusterSequence = List.of(startCluster);
        } else {
            clusterSequence = clusterAstar(graph, startCluster, goalCluster, maxNodes);
            if (clusterSequence == null) {
                return new PathResult.Unreachable(requester,
                    "No cluster path from " + startCluster + " to " + goalCluster);
            }
        }

        List<NavPoint> waypoints = new ArrayList<>();
        waypoints.add(start);

        int[] nodeCount = {0};
        boolean truncated = false;

        for (int i = 0; i < clusterSequence.size(); i++) {
            int cid = clusterSequence.get(i);
            NavPoint segStart = waypoints.get(waypoints.size() - 1);
            NavPoint segGoal = (i == clusterSequence.size() - 1)
                ? goal
                : graph.getCluster(clusterSequence.get(i + 1))
                    .map(HpaCluster::centroid)
                    .orElse(goal);

            List<NavPoint> segment = localAstar(mesh, cid, segStart, segGoal,
                maxNodes - nodeCount[0], nodeCount);

            if (segment == null) {
                truncated = true;
                break;
            }
            for (int j = 1; j < segment.size(); j++) {
                waypoints.add(segment.get(j));
            }
        }

        if (!waypoints.isEmpty() && waypoints.get(waypoints.size() - 1)
            .distanceTo(goal) > 0.01f && !truncated) {
            waypoints.add(goal);
        }

        float totalCost = computePathCost(waypoints);
        NavPath path = new NavPath(Collections.unmodifiableList(waypoints),
            totalCost, !truncated);

        if (truncated) {
            log.debug("Path for {} truncated at node cap {}", requester, maxNodes);
            return new PathResult.Partial(requester, path, "Node cap reached");
        }
        return new PathResult.Found(requester, path);
    }

    private List<Integer> clusterAstar(HpaGraph graph, int startCid, int goalCid,
                                       int maxNodes) {
        HpaCluster goalCluster = graph.getCluster(goalCid).orElse(null);
        if (goalCluster == null) {
            return null;
        }

        Map<Integer, Float> gScore = new HashMap<>();
        Map<Integer, Float> fScore = new HashMap<>();
        Map<Integer, Integer> cameFrom = new HashMap<>();
        PriorityQueue<Integer> open = new PriorityQueue<>(
            Comparator.comparingDouble(id -> fScore.getOrDefault(id, Float.MAX_VALUE)));
        Set<Integer> closed = new HashSet<>();

        gScore.put(startCid, 0f);
        fScore.put(startCid, heuristicCluster(graph, startCid, goalCid));
        open.add(startCid);

        int expanded = 0;
        while (!open.isEmpty() && expanded < maxNodes) {
            int current = open.poll();
            if (current == goalCid) {
                return reconstructClusterPath(cameFrom, current);
            }
            closed.add(current);
            expanded++;

            HpaCluster cluster = graph.getCluster(current).orElse(null);
            if (cluster == null) {
                continue;
            }

            for (Map.Entry<Integer, Float> edge : cluster.neighborCosts().entrySet()) {
                int neighbor = edge.getKey();
                if (closed.contains(neighbor)) {
                    continue;
                }
                float tentativeG = gScore.getOrDefault(current, Float.MAX_VALUE)
                    + edge.getValue();
                if (tentativeG < gScore.getOrDefault(neighbor, Float.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    fScore.put(neighbor, tentativeG + heuristicCluster(graph, neighbor, goalCid));
                    open.remove(neighbor);
                    open.add(neighbor);
                }
            }
        }
        return null;
    }

    private float heuristicCluster(HpaGraph graph, int fromCid, int toCid) {
        NavPoint a = graph.getCluster(fromCid).map(HpaCluster::centroid)
            .orElse(NavPoint.of(0, 0, 0));
        NavPoint b = graph.getCluster(toCid).map(HpaCluster::centroid)
            .orElse(NavPoint.of(0, 0, 0));
        return a.distanceTo(b);
    }

    private List<Integer> reconstructClusterPath(Map<Integer, Integer> cameFrom, int current) {
        LinkedList<Integer> path = new LinkedList<>();
        path.addFirst(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }
        return path;
    }

    private List<NavPoint> localAstar(NavMesh mesh, int clusterId,
                                      NavPoint segStart, NavPoint segGoal,
                                      int remainingNodes, int[] nodeCount) {
        Optional<NavPoly> startPolyOpt = closestPolyInCluster(mesh, clusterId, segStart);
        Optional<NavPoly> goalPolyOpt = closestPolyInCluster(mesh, clusterId, segGoal);

        if (startPolyOpt.isEmpty() || goalPolyOpt.isEmpty()) {
            return List.of(segStart, segGoal);
        }

        int startId = startPolyOpt.get().id();
        int goalId = goalPolyOpt.get().id();

        if (startId == goalId) {
            return List.of(segStart, segGoal);
        }

        Map<Integer, Float> gScore = new HashMap<>();
        Map<Integer, Float> fScore = new HashMap<>();
        Map<Integer, Integer> cameFrom = new HashMap<>();
        PriorityQueue<Integer> open = new PriorityQueue<>(
            Comparator.comparingDouble(id -> fScore.getOrDefault(id, Float.MAX_VALUE)));
        Set<Integer> closed = new HashSet<>();

        gScore.put(startId, 0f);
        fScore.put(startId, segStart.distanceTo(segGoal));
        open.add(startId);

        while (!open.isEmpty()) {
            if (nodeCount[0] >= remainingNodes) {
                return null;
            }

            int current = open.poll();
            nodeCount[0]++;

            if (current == goalId) {
                return reconstructLocalPath(mesh, cameFrom, current, startId, segStart, segGoal);
            }
            closed.add(current);

            NavPoly currentPoly = mesh.getPoly(current).orElse(null);
            if (currentPoly == null) {
                continue;
            }

            for (int neighborId : currentPoly.neighborIds()) {
                if (closed.contains(neighborId)) {
                    continue;
                }
                NavPoly neighborPoly = mesh.getPoly(neighborId).orElse(null);
                if (neighborPoly == null) {
                    continue;
                }

                float tentativeG = gScore.getOrDefault(current, Float.MAX_VALUE)
                    + currentPoly.costTo(neighborPoly);
                if (tentativeG < gScore.getOrDefault(neighborId, Float.MAX_VALUE)) {
                    cameFrom.put(neighborId, current);
                    gScore.put(neighborId, tentativeG);
                    fScore.put(neighborId, tentativeG + neighborPoly.centroid().distanceTo(segGoal));
                    open.remove(neighborId);
                    open.add(neighborId);
                }
            }
        }
        return List.of(segStart, segGoal);
    }

    private Optional<NavPoly> closestPolyInCluster(NavMesh mesh, int clusterId,
                                                   NavPoint pt) {
        return mesh.polysInCluster(clusterId).stream()
            .min(Comparator.comparingDouble(p -> p.centroid().distanceTo(pt)));
    }

    private List<NavPoint> reconstructLocalPath(NavMesh mesh,
                                                Map<Integer, Integer> cameFrom,
                                                int current, int startId,
                                                NavPoint segStart, NavPoint segGoal) {
        LinkedList<NavPoint> path = new LinkedList<>();
        path.addFirst(segGoal);
        while (cameFrom.containsKey(current)) {
            mesh.getPoly(current).ifPresent(p -> path.addFirst(p.centroid()));
            current = cameFrom.get(current);
        }
        path.addFirst(segStart);
        return path;
    }

    private float computePathCost(List<NavPoint> waypoints) {
        float cost = 0;
        for (int i = 1; i < waypoints.size(); i++) {
            cost += waypoints.get(i - 1).distanceTo(waypoints.get(i));
        }
        return cost;
    }
}
