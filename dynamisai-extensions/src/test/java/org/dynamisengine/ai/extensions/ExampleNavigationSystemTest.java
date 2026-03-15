package org.dynamisengine.ai.extensions;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.navigation.NavPoint;
import org.dynamisengine.ai.navigation.PathRequest;
import org.dynamisengine.ai.navigation.RvoAgent;
import org.dynamisengine.ai.testkit.NavigationSystemContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleNavigationSystemTest extends NavigationSystemContractTest {

    @Override
    protected NavigationSystem createSubject() {
        return new ExampleNavigationSystem();
    }

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleNavigationSystem nav = new ExampleNavigationSystem();
        EntityId id = EntityId.of(42L);
        assertTrue(nav.isReady());
        assertDoesNotThrow(() -> nav.requestPath(PathRequest.of(id, new Location(0, 0, 0), new Location(1, 0, 1))).get());
        assertNotNull(nav.steer(id, new Location(0, 0, 0), 1f));
        nav.updateAgentState(RvoAgent.of(id, NavPoint.of(0f, 0f, 0f), 0.5f, 1f));
        nav.removeAgent(id);
    }
}
