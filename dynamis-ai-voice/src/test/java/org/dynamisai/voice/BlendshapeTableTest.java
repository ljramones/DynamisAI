package org.dynamisai.voice;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BlendshapeTableTest {

    @Test
    void defaultHumanoidBuilds() {
        assertDoesNotThrow(BlendshapeTable::defaultHumanoid);
    }

    @Test
    void defaultHumanoidContainsAllPrestonBlairLabels() {
        BlendshapeTable table = BlendshapeTable.defaultHumanoid();
        Set<String> labels = table.visemeWeights().keySet();
        assertTrue(labels.containsAll(Set.of(
            "rest", "MBP", "etc", "AI", "O", "E", "U", "WQ", "FV", "L", "Th"
        )));
    }

    @Test
    void affectMapsAreNonEmpty() {
        BlendshapeTable table = BlendshapeTable.defaultHumanoid();
        assertFalse(table.affectValencePos().isEmpty());
        assertFalse(table.affectValenceNeg().isEmpty());
        assertFalse(table.affectArousal().isEmpty());
        assertFalse(table.affectDominance().isEmpty());
        assertFalse(table.affectSarcasm().isEmpty());
    }
}
