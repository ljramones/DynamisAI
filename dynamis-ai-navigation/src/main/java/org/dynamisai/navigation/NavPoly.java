package org.dynamisai.navigation;

import java.util.List;

/**
 * A convex polygon in the NavMesh.
 * Vertices are stored in counter-clockwise order.
 * Neighbor IDs reference connected NavPolys in the same NavMesh.
 */
public record NavPoly(
    int id,
    List<NavPoint> vertices,
    NavPoint centroid,
    List<Integer> neighborIds,
    float traversalCost,
    int clusterId
) {
    public NavPoly {
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("NavPoly must have at least 3 vertices");
        }
        if (traversalCost <= 0) {
            throw new IllegalArgumentException("traversalCost must be > 0");
        }
    }

    /** Euclidean distance between centroids — used as A* edge cost. */
    public float costTo(NavPoly other) {
        return centroid.distanceTo(other.centroid) * traversalCost;
    }

    /** Compute centroid from vertex list. */
    public static NavPoint computeCentroid(List<NavPoint> vertices) {
        float sx = 0;
        float sy = 0;
        float sz = 0;
        for (NavPoint v : vertices) {
            sx += v.x();
            sy += v.y();
            sz += v.z();
        }
        int n = vertices.size();
        return NavPoint.of(sx / n, sy / n, sz / n);
    }
}
