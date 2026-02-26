package org.dynamisai.navigation;

/**
 * Two-level HPA* pathfinder.
 * Level 1: A* on HpaGraph (cluster-to-cluster).
 * Level 0: A* on NavMesh polys within each cluster segment.
 */
public interface HpaPathfinder {

    /**
     * Find a path from start to goal on the given NavMesh.
     *
     * @param mesh     NavMesh to search
     * @param graph    Pre-built HPA* abstract graph
     * @param start    Start position
     * @param goal     Goal position
     * @param maxNodes Hard node expansion cap — prevents runaway search
     * @return PathResult — Found, Unreachable, or Partial
     */
    PathResult findPath(NavMesh mesh, HpaGraph graph,
                        NavPoint start, NavPoint goal,
                        int maxNodes,
                        org.dynamisai.core.EntityId requester);
}
