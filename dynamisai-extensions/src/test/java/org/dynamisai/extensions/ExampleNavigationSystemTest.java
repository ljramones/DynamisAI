package org.dynamisai.extensions;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.navigation.NavigationSystem;
import org.dynamisai.navigation.NavPoint;
import org.dynamisai.navigation.PathRequest;
import org.dynamisai.navigation.RvoAgent;
import org.dynamisai.testkit.NavigationSystemContractTest;
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
