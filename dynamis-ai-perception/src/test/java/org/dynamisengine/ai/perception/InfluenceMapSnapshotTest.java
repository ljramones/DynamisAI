package org.dynamisengine.ai.perception;

import org.dynamisengine.ai.core.Location;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class InfluenceMapSnapshotTest {

    @Test
    void sampleReturnsCorrectValueForKnownLayerAndPosition() {
        EnumMap<InfluenceLayer, float[]> layers = new EnumMap<>(InfluenceLayer.class);
        float[] threat = new float[9];
        threat[4] = 0.9f;
        layers.put(InfluenceLayer.THREAT, threat);

        InfluenceMapSnapshot snapshot = new InfluenceMapSnapshot(layers, 3, 3, 1f, 10L);
        float v = snapshot.sample(InfluenceLayer.THREAT, new Location(1.5f, 0f, 1.5f));
        assertEquals(0.9f, v, 1e-6f);
    }

    @Test
    void highestCellReturnsMaxLocation() {
        EnumMap<InfluenceLayer, float[]> layers = new EnumMap<>(InfluenceLayer.class);
        float[] threat = new float[16];
        threat[14] = 0.95f;
        layers.put(InfluenceLayer.THREAT, threat);

        InfluenceMapSnapshot snapshot = new InfluenceMapSnapshot(layers, 4, 4, 2f, 20L);
        Location max = snapshot.highestCell(InfluenceLayer.THREAT);

        assertEquals(5f, max.x(), 1e-6f);
        assertEquals(7f, max.z(), 1e-6f);
    }

    @Test
    void lowestCellNearReturnsLowestInRadius() {
        EnumMap<InfluenceLayer, float[]> layers = new EnumMap<>(InfluenceLayer.class);
        float[] threat = new float[25];
        for (int i = 0; i < threat.length; i++) {
            threat[i] = 0.8f;
        }
        threat[6] = 0.1f;
        layers.put(InfluenceLayer.THREAT, threat);

        InfluenceMapSnapshot snapshot = new InfluenceMapSnapshot(layers, 5, 5, 1f, 30L);
        Location low = snapshot.lowestCellNear(InfluenceLayer.THREAT, new Location(1.5f, 0f, 1.5f), 2f);

        assertEquals(1.5f, low.x(), 1e-6f);
        assertEquals(1.5f, low.z(), 1e-6f);
    }

    @Test
    void lowestCellNearWithTinyRadiusReturnsNearestCell() {
        EnumMap<InfluenceLayer, float[]> layers = new EnumMap<>(InfluenceLayer.class);
        float[] threat = new float[9];
        threat[4] = 0.2f;
        layers.put(InfluenceLayer.THREAT, threat);

        InfluenceMapSnapshot snapshot = new InfluenceMapSnapshot(layers, 3, 3, 1f, 40L);
        Location low = snapshot.lowestCellNear(InfluenceLayer.THREAT, new Location(1.5f, 0f, 1.5f), 0.1f);

        assertEquals(1.5f, low.x(), 1e-6f);
        assertEquals(1.5f, low.z(), 1e-6f);
    }

    @Test
    void layersMapAndArraysAreDefensivelyCopied() {
        EnumMap<InfluenceLayer, float[]> source = new EnumMap<>(InfluenceLayer.class);
        float[] threat = new float[4];
        threat[0] = 0.4f;
        source.put(InfluenceLayer.THREAT, threat);

        InfluenceMapSnapshot snapshot = new InfluenceMapSnapshot(source, 2, 2, 1f, 50L);

        threat[0] = 1f;
        assertNotEquals(1f, snapshot.layers().get(InfluenceLayer.THREAT)[0]);
        assertThrows(UnsupportedOperationException.class,
            () -> snapshot.layers().put(InfluenceLayer.COVER, new float[4]));
    }
}
