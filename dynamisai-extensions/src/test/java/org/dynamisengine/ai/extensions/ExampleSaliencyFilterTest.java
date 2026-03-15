package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.core.WorldFacts;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExampleSaliencyFilterTest {

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleSaliencyFilter filter = new ExampleSaliencyFilter();
        EntityId owner = EntityId.of(1L);
        WorldFacts facts = new WorldFacts(
            Map.of("weather", "clear"),
            List.of(EntityId.of(2L), EntityId.of(3L)),
            ThreatLevel.LOW,
            new Location(1, 0, 1),
            null);

        var snapshot = filter.filter(owner, facts, AffectVector.neutral(), 1L);
        assertNotNull(snapshot);
        assertEquals(owner, snapshot.owner());
        assertEquals(2, snapshot.percepts().size());
    }
}
