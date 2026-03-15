package org.dynamisengine.ai.perception;

import org.dynamisengine.ai.core.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfluenceGridTest {

    @Test
    void setAndGetRoundTrip() {
        InfluenceGrid grid = new InfluenceGrid(8, 8, 1f);
        grid.set(3, 4, 0.7f);
        assertEquals(0.7f, grid.get(3, 4), 1e-6f);
    }

    @Test
    void outOfBoundsGetReturnsZero() {
        InfluenceGrid grid = new InfluenceGrid(8, 8, 1f);
        assertEquals(0f, grid.get(-1, 0), 1e-6f);
        assertEquals(0f, grid.get(0, -1), 1e-6f);
        assertEquals(0f, grid.get(99, 0), 1e-6f);
        assertEquals(0f, grid.get(0, 99), 1e-6f);
    }

    @Test
    void decayAllHalvesAllCells() {
        InfluenceGrid grid = new InfluenceGrid(4, 4, 1f);
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 4; x++) {
                grid.set(x, z, 1f);
            }
        }
        grid.decayAll(0.5f);
        for (float v : grid.rawSnapshot()) {
            assertEquals(0.5f, v, 1e-6f);
        }
    }

    @Test
    void decayAllMatchesScalarReference() {
        InfluenceGrid grid = new InfluenceGrid(17, 13, 1f);
        int i = 0;
        for (int z = 0; z < 13; z++) {
            for (int x = 0; x < 17; x++) {
                grid.set(x, z, (i++ % 10) / 10f);
            }
        }
        float[] before = grid.rawSnapshot();
        grid.decayAll(0.37f);
        float[] after = grid.rawSnapshot();

        for (int idx = 0; idx < before.length; idx++) {
            assertEquals(before[idx] * 0.37f, after[idx], 1e-6f);
        }
    }

    @Test
    void addRadialInfluenceAffectsCentreAndBoundary() {
        InfluenceGrid grid = new InfluenceGrid(11, 11, 1f);
        Location centre = grid.cellCentre(5, 5);
        grid.addRadialInfluence(centre, 3f, 1f);

        assertTrue(grid.get(5, 5) > 0.99f);
        assertTrue(grid.get(8, 5) < 0.05f);
    }

    @Test
    void addRadialInfluenceLeavesBeyondRadiusUnchanged() {
        InfluenceGrid grid = new InfluenceGrid(11, 11, 1f);
        Location centre = grid.cellCentre(2, 2);
        grid.addRadialInfluence(centre, 2f, 0.8f);

        assertEquals(0f, grid.get(10, 10), 1e-6f);
    }

    @Test
    void propagateSpreadsSpikeToNeighbours() {
        InfluenceGrid grid = new InfluenceGrid(7, 7, 1f);
        grid.set(3, 3, 1f);

        grid.propagate(0.6f, 0.1f);

        assertTrue(grid.get(3, 2) > 0f);
        assertTrue(grid.get(3, 4) > 0f);
        assertTrue(grid.get(2, 3) > 0f);
        assertTrue(grid.get(4, 3) > 0f);
    }

    @Test
    void sampleAtExactCellCentreReturnsCellValue() {
        InfluenceGrid grid = new InfluenceGrid(6, 6, 1f);
        grid.set(2, 2, 0.66f);

        assertEquals(0.66f, grid.sampleAt(grid.cellCentre(2, 2)), 1e-6f);
    }

    @Test
    void worldToCellRoundTrip() {
        InfluenceGrid grid = new InfluenceGrid(10, 10, 2f);
        int[] cell = grid.worldToCell(grid.cellCentre(7, 3));
        assertEquals(7, cell[0]);
        assertEquals(3, cell[1]);
    }

    @Test
    void addRadialInfluenceClampsToUpperBound() {
        InfluenceGrid grid = new InfluenceGrid(6, 6, 1f);
        Location centre = grid.cellCentre(3, 3);
        grid.set(3, 3, 1f);

        grid.addRadialInfluence(centre, 1f, 1f);

        assertEquals(1f, grid.get(3, 3), 1e-6f);
    }
}
