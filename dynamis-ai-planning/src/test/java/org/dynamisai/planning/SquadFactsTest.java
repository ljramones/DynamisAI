package org.dynamisai.planning;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SquadFactsTest {

    @Test
    void constantsAreNonNull() {
        for (String value : values()) {
            assertNotNull(value);
        }
    }

    @Test
    void constantsAreNonEmpty() {
        for (String value : values()) {
            assertFalse(value.isBlank());
        }
    }

    @Test
    void constantsAreUnique() {
        List<String> values = values();
        assertEquals(values.size(), Set.copyOf(values).size());
    }

    private static List<String> values() {
        return List.of(
            SquadFacts.ROLE_SUPPRESSOR,
            SquadFacts.ROLE_FLANKER,
            SquadFacts.ROLE_BREACHER,
            SquadFacts.ROLE_MEDIC,
            SquadFacts.ROLE_SPOTTER,
            SquadFacts.THREAT_POSITION,
            SquadFacts.THREAT_ENTITY,
            SquadFacts.RALLY_POINT,
            SquadFacts.BACKUP_REQUESTED,
            SquadFacts.PLAN_PHASE
        );
    }
}
