package org.dynamisai.voice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisemeExtractorTest {

    private VisemeExtractor extractor;

    @BeforeEach
    void setUp() { extractor = new VisemeExtractor(); }

    @Test
    void emptyTextReturnsEmptyList() {
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
        assertTrue(extractor.extract("   ").isEmpty());
    }

    @Test
    void singleWordProducesVisemes() {
        List<VisemeTimestamp> visemes = extractor.extract("hello");
        assertFalse(visemes.isEmpty());
    }

    @Test
    void visemeOffsetsAreStrictlyNonDecreasing() {
        List<VisemeTimestamp> visemes = extractor.extract("the quick brown fox");
        for (int i = 1; i < visemes.size(); i++) {
            assertFalse(visemes.get(i).offset().compareTo(visemes.get(i - 1).offset()) < 0,
                "Offset at " + i + " decreased");
        }
    }

    @Test
    void allVisemeWeightsAreInRange() {
        List<VisemeTimestamp> visemes = extractor.extract("speaking naturally");
        for (VisemeTimestamp v : visemes) {
            assertTrue(v.weight() >= 0f && v.weight() <= 1f,
                "Weight out of range: " + v.weight());
        }
    }

    @Test
    void bilabialConsonantsMapToM() {
        List<VisemeTimestamp> visemes = extractor.extract("mom");
        assertTrue(visemes.stream().anyMatch(v -> v.viseme().equals("M")),
            "Expected M viseme for bilabial consonants");
    }

    @Test
    void labiodentalsMapToF() {
        List<VisemeTimestamp> visemes = extractor.extract("five");
        assertTrue(visemes.stream().anyMatch(v -> v.viseme().equals("F")),
            "Expected F viseme for f/v sounds");
    }

    @Test
    void sibilantsMapToSS() {
        List<VisemeTimestamp> visemes = extractor.extract("sister");
        assertTrue(visemes.stream().anyMatch(v -> v.viseme().equals("SS")),
            "Expected SS viseme for sibilants");
    }

    @Test
    void listIsImmutable() {
        List<VisemeTimestamp> visemes = extractor.extract("hello");
        assertThrows(UnsupportedOperationException.class, () -> visemes.add(null));
    }

    @Test
    void longerTextProducesMoreVisemes() {
        int short_ = extractor.extract("hi").size();
        int long_ = extractor.extract("hello there how are you doing today").size();
        assertTrue(long_ > short_);
    }

    @Test
    void estimatedDurationIsPositive() {
        Duration d = extractor.estimateDuration("Hello, how are you?");
        assertTrue(d.toMillis() > 0);
    }

    @Test
    void estimatedDurationScalesWithLength() {
        Duration short_ = extractor.estimateDuration("hi");
        Duration long_ = extractor.estimateDuration("the quick brown fox jumped over the lazy dog");
        assertTrue(long_.compareTo(short_) > 0);
    }

    @Test
    void punctuationDoesNotProduceVisemes() {
        List<VisemeTimestamp> visemes = extractor.extract("...");
        assertTrue(visemes.isEmpty());
    }
}
