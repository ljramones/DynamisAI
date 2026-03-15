package org.dynamisengine.ai.testkit;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.SteeringOutput;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.navigation.PathRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class NavigationSystemContractTest {

    protected abstract NavigationSystem createSubject();

    @Test
    void steerWithoutPathReturnsOutput() {
        NavigationSystem nav = createSubject();
        EntityId agent = EntityId.of(1L);
        SteeringOutput out = nav.steer(agent, new Location(0, 0, 0), 4f);
        assertNotNull(out);
    }

    @Test
    void requestPathReturnsNonNullFuture() {
        NavigationSystem nav = createSubject();
        var future = nav.requestPath(PathRequest.of(
            EntityId.of(1L),
            new Location(0, 0, 0),
            new Location(10, 0, 10)
        ));
        assertNotNull(future);
    }

    @Test
    void removeAgentIsIdempotent() {
        NavigationSystem nav = createSubject();
        EntityId agent = EntityId.of(1L);
        assertDoesNotThrow(() -> {
            nav.removeAgent(agent);
            nav.removeAgent(agent);
        });
    }
}
