package org.dynamisengine.ai.voice;

import org.dynamisengine.ai.cognition.AffectVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlendshapeMapperTest {

    private BlendshapeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BlendshapeMapper(BlendshapeTable.defaultHumanoid());
    }

    @Test
    void emptyVisemeListReturnsEmptyMapResult() {
        assertTrue(mapper.map(List.of(), new AffectVector(0f, 0f, 0f, 0f, 0f)).isEmpty());
    }

    @Test
    void singleAiVisemeNeutralAffectHasExpectedJawOpen() {
        List<BlendshapeFrame> frames = mapper.map(
            List.of(new VisemeTimestamp(Duration.ZERO, "AI", 0.85f)),
            new AffectVector(0f, 0f, 0f, 0f, 0f)
        );
        assertEquals(1, frames.size());
        assertEquals(0.7f * 0.85f, frames.get(0).weights().get("jawOpen"), 0.001f);
    }

    @Test
    void positiveValenceAddsSmile() {
        List<BlendshapeFrame> frames = mapper.map(
            List.of(new VisemeTimestamp(Duration.ZERO, "rest", 0.85f)),
            new AffectVector(1.0f, 0f, 0f, 0f, 1.0f)
        );
        assertTrue(frames.get(0).weights().get("mouthSmileL") > 0f);
    }

    @Test
    void negativeValenceAddsFrown() {
        List<BlendshapeFrame> frames = mapper.map(
            List.of(new VisemeTimestamp(Duration.ZERO, "rest", 0.85f)),
            new AffectVector(-1.0f, 0f, 0f, 0f, 1.0f)
        );
        assertTrue(frames.get(0).weights().get("mouthFrownL") > 0f);
    }

    @Test
    void weightsAreAlwaysClampedToUnitRange() {
        List<BlendshapeFrame> frames = mapper.map(
            List.of(new VisemeTimestamp(Duration.ZERO, "AI", 1.0f)),
            new AffectVector(1.0f, 1.0f, 1.0f, 1.0f, 1.0f)
        );
        frames.get(0).weights().weights().values().forEach(v -> {
            assertTrue(v >= 0f && v <= 1f);
        });
    }

    @Test
    void mapAtTimeBeforeFirstReturnsFirst() {
        List<VisemeTimestamp> visemes = List.of(
            new VisemeTimestamp(Duration.ofMillis(200), "rest", 1.0f),
            new VisemeTimestamp(Duration.ofMillis(700), "AI", 1.0f)
        );
        BlendshapeFrame first = mapper.map(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f)).get(0);
        BlendshapeFrame at = mapper.mapAtTime(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f), 0.1f);
        assertEquals(first.weights().weights(), at.weights().weights());
    }

    @Test
    void mapAtTimeAfterLastReturnsLast() {
        List<VisemeTimestamp> visemes = List.of(
            new VisemeTimestamp(Duration.ZERO, "rest", 1.0f),
            new VisemeTimestamp(Duration.ofSeconds(1), "AI", 1.0f)
        );
        List<BlendshapeFrame> frames = mapper.map(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f));
        BlendshapeFrame at = mapper.mapAtTime(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f), 2.0f);
        assertEquals(frames.get(frames.size() - 1).weights().weights(), at.weights().weights());
    }

    @Test
    void mapAtTimeExactFrameReturnsSameWeights() {
        List<VisemeTimestamp> visemes = List.of(
            new VisemeTimestamp(Duration.ZERO, "rest", 1.0f),
            new VisemeTimestamp(Duration.ofSeconds(1), "AI", 1.0f)
        );
        List<BlendshapeFrame> frames = mapper.map(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f));
        BlendshapeFrame at = mapper.mapAtTime(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f), 1.0f);
        assertEquals(frames.get(1).weights().weights(), at.weights().weights());
    }

    @Test
    void mapAtTimeMidpointInterpolatesJawOpen() {
        List<VisemeTimestamp> visemes = List.of(
            new VisemeTimestamp(Duration.ZERO, "rest", 1.0f),
            new VisemeTimestamp(Duration.ofSeconds(1), "AI", 1.0f)
        );
        BlendshapeFrame at = mapper.mapAtTime(visemes, new AffectVector(0f, 0f, 0f, 0f, 0f), 0.5f);
        assertEquals(0.35f, at.weights().get("jawOpen"), 0.001f);
    }
}
