package org.dynamisengine.ai.navigation;

import java.util.List;
import java.util.Map;

/**
 * Abstract HPA* node — a group of NavPolys treated as a single node
 * at the cluster level of the hierarchy.
 */
public record HpaCluster(
    int clusterId,
    NavPoint centroid,
    List<Integer> polyIds,
    Map<Integer, Float> neighborCosts
) {}
