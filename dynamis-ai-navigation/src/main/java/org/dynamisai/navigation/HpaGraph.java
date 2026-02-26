package org.dynamisai.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract HPA* graph — one node per NavMesh cluster.
 * Built once from NavMesh; immutable after construction.
 */
public final class HpaGraph {

    private final Map<Integer, HpaCluster> clusters;

    private HpaGraph(Map<Integer, HpaCluster> clusters) {
        this.clusters = Collections.unmodifiableMap(clusters);
    }

    public Optional<HpaCluster> getCluster(int id) {
        return Optional.ofNullable(clusters.get(id));
    }

    public Collection<HpaCluster> allClusters() {
        return clusters.values();
    }

    public int clusterCount() {
        return clusters.size();
    }

    /**
     * Build abstract graph from NavMesh.
     * Cluster centroid = average of member poly centroids.
     * Abstract edge exists between clusters if any poly in cluster A
     * has a neighbor in cluster B.
     * Abstract edge cost = minimum concrete edge cost between the clusters.
     */
    public static HpaGraph build(NavMesh mesh) {
        Map<Integer, List<NavPoly>> byCluster = new LinkedHashMap<>();
        for (NavPoly p : mesh.allPolys()) {
            byCluster.computeIfAbsent(p.clusterId(), k -> new ArrayList<>()).add(p);
        }

        Map<Integer, HpaCluster> clusters = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<NavPoly>> entry : byCluster.entrySet()) {
            int cid = entry.getKey();
            List<NavPoly> members = entry.getValue();

            float sx = 0;
            float sy = 0;
            float sz = 0;
            for (NavPoly p : members) {
                sx += p.centroid().x();
                sy += p.centroid().y();
                sz += p.centroid().z();
            }
            NavPoint centroid = NavPoint.of(sx / members.size(), sy / members.size(), sz / members.size());

            List<Integer> polyIds = members.stream().map(NavPoly::id).toList();

            Map<Integer, Float> neighborCosts = new LinkedHashMap<>();
            for (NavPoly p : members) {
                for (int nid : p.neighborIds()) {
                    mesh.getPoly(nid).ifPresent(neighbor -> {
                        if (neighbor.clusterId() != cid) {
                            float cost = p.costTo(neighbor);
                            neighborCosts.merge(neighbor.clusterId(), cost, Math::min);
                        }
                    });
                }
            }
            clusters.put(cid, new HpaCluster(cid, centroid, polyIds, neighborCosts));
        }
        return new HpaGraph(clusters);
    }
}
