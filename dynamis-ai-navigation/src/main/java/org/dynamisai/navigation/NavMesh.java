package org.dynamisai.navigation;

import org.dynamisai.core.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Polygon-based navigation mesh.
 * Immutable after construction — built via NavMeshBuilder.
 * Spatial lookup uses a flat list scan (sufficient for < 10k polys).
 * Replace with spatial hash or BVH for production-scale worlds.
 */
public final class NavMesh {

    private final Map<Integer, NavPoly> polys;
    private final int clusterCount;

    NavMesh(Map<Integer, NavPoly> polys, int clusterCount) {
        this.polys = Collections.unmodifiableMap(polys);
        this.clusterCount = clusterCount;
    }

    public Optional<NavPoly> getPoly(int id) {
        return Optional.ofNullable(polys.get(id));
    }

    public Collection<NavPoly> allPolys() {
        return polys.values();
    }

    public int polyCount() {
        return polys.size();
    }

    public int clusterCount() {
        return clusterCount;
    }

    /**
     * Find the nearest NavPoly centroid to a world position.
     * Returns empty if the mesh is empty.
     */
    public Optional<NavPoly> nearestPoly(Location loc) {
        NavPoint pt = NavPoint.from(loc);
        return polys.values().stream()
            .min(Comparator.comparingDouble(p -> p.centroid().distanceTo(pt)));
    }

    /**
     * Find the nearest NavPoly centroid to a NavPoint.
     */
    public Optional<NavPoly> nearestPoly(NavPoint pt) {
        return polys.values().stream()
            .min(Comparator.comparingDouble(p -> p.centroid().distanceTo(pt)));
    }

    /** All polys in a given cluster. */
    public List<NavPoly> polysInCluster(int clusterId) {
        return polys.values().stream()
            .filter(p -> p.clusterId() == clusterId)
            .toList();
    }
}
