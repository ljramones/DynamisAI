package org.dynamisai.navigation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent NavMesh builder.
 *
 * For tests and tools: buildGrid() produces a regular grid NavMesh
 * with automatic neighbor wiring and cluster assignment.
 * addPoly() supports hand-crafted meshes for unit tests.
 */
public final class NavMeshBuilder {

    private final Map<Integer, NavPoly> polys = new LinkedHashMap<>();
    private int nextId = 0;

    /**
     * Build a flat regular grid NavMesh.
     *
     * @param cols        number of columns (X axis)
     * @param rows        number of rows (Z axis)
     * @param cellSize    world-unit size of each cell
     * @param clusterSize polys per cluster edge (e.g. 4 = 4x4 clusters)
     */
    public static NavMesh buildGrid(int cols, int rows, float cellSize, int clusterSize) {
        NavMeshBuilder b = new NavMeshBuilder();
        int[][] ids = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float x0 = col * cellSize;
                float z0 = row * cellSize;
                float x1 = x0 + cellSize;
                float z1 = z0 + cellSize;
                List<NavPoint> verts = List.of(
                    NavPoint.of(x0, 0, z0),
                    NavPoint.of(x1, 0, z0),
                    NavPoint.of(x1, 0, z1),
                    NavPoint.of(x0, 0, z1)
                );
                int clusterId = (row / clusterSize) * ((cols + clusterSize - 1) / clusterSize)
                    + (col / clusterSize);
                int id = b.nextId++;
                ids[row][col] = id;
                b.polys.put(id, new NavPoly(id, verts,
                    NavPoly.computeCentroid(verts), new ArrayList<>(), 1.0f, clusterId));
            }
        }

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int id = ids[row][col];
                NavPoly poly = b.polys.get(id);
                List<Integer> neighbors = poly.neighborIds();
                for (int[] d : dirs) {
                    int nr = row + d[0];
                    int nc = col + d[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        neighbors.add(ids[nr][nc]);
                    }
                }
            }
        }

        int clusterCols = (cols + clusterSize - 1) / clusterSize;
        int clusterRows = (rows + clusterSize - 1) / clusterSize;
        return new NavMesh(b.polys, clusterCols * clusterRows);
    }

    /** Add a custom polygon — for hand-crafted test meshes. */
    public NavMeshBuilder addPoly(List<NavPoint> vertices, List<Integer> neighbors,
                                  float traversalCost, int clusterId) {
        int id = nextId++;
        polys.put(id, new NavPoly(id, vertices,
            NavPoly.computeCentroid(vertices), new ArrayList<>(neighbors),
            traversalCost, clusterId));
        return this;
    }

    public NavMesh build(int clusterCount) {
        return new NavMesh(polys, clusterCount);
    }
}
