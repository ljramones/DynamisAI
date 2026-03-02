package org.dynamisai.lod;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.LodTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportanceScoreTest {

    @Test
    void recordConstructionAndEquality() {
        ImportanceScore a = new ImportanceScore(EntityId.of(1L), 0.5f, LodTier.TIER_1, 10L);
        ImportanceScore b = new ImportanceScore(EntityId.of(1L), 0.5f, LodTier.TIER_1, 10L);
        assertEquals(a, b);
        assertEquals(0.5f, a.score(), 0.0001f);
    }
}
