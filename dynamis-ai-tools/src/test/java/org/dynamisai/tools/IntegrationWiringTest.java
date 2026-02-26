package org.dynamisai.tools;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueRequest;
import org.dynamisai.core.AITaskNode;
import org.dynamisai.core.DefaultBudgetGovernor;
import org.dynamisai.core.DefaultWorldStateStore;
import org.dynamisai.core.DegradeMode;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.FrameBudgetReport;
import org.dynamisai.core.Location;
import org.dynamisai.core.Priority;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.crowd.DefaultCrowdSystem;
import org.dynamisai.crowd.FormationType;
import org.dynamisai.crowd.GroupId;
import org.dynamisai.navigation.DefaultNavigationSystem;
import org.dynamisai.navigation.NavMeshBuilder;
import org.dynamisai.navigation.NavigationSystem;
import org.dynamisai.social.DefaultSocialSystem;
import org.dynamisai.social.SocialDialogueShaper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the three runtime wires from Task 17 are in place.
 */
class IntegrationWiringTest {

    @Test
    void navigationSystemIsReady() {
        NavigationSystem nav = new DefaultNavigationSystem(
            NavMeshBuilder.buildGrid(4, 4, 2f, 2));
        assertTrue(nav.isReady());
        ((DefaultNavigationSystem) nav).shutdown();
    }

    @Test
    void socialDialogueShaperWiresWithoutThrowing() {
        DefaultSocialSystem social = new DefaultSocialSystem();
        EntityId npc = EntityId.of(1L);
        EntityId player = EntityId.of(2L);
        DialogueRequest request = new DialogueRequest(
            npc, player, "Hello.",
            new WorldFacts(Map.of(), List.of(), ThreatLevel.NONE,
                new Location(0, 0, 0), null),
            AffectVector.neutral(),
            null);
        assertDoesNotThrow(() -> SocialDialogueShaper.shape(request, social));
    }

    @Test
    void crowdSystemTickRegistersInBudgetGovernor() {
        DefaultCrowdSystem crowd = new DefaultCrowdSystem();
        DefaultBudgetGovernor governor = new DefaultBudgetGovernor(16);
        DefaultWorldStateStore store = new DefaultWorldStateStore();

        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, EntityId.of(1L), new Location(1, 0, 1));

        governor.register(new AITaskNode(
            "crowd-tick", 3, Priority.NORMAL, DegradeMode.DEFER,
            () -> crowd.tick(1L, 0.016f),
            () -> {}
        ));

        FrameBudgetReport report = governor.getLastFrameReport();
        assertNull(report);
        governor.runFrame(1L, store.getCurrentSnapshot());
        assertNotNull(governor.getLastFrameReport());
        assertEquals(1, crowd.latestSnapshot().totalAgents());
    }

    @Test
    void crowdTickProducesSnapshotWithRegisteredAgent() {
        DefaultCrowdSystem crowd = new DefaultCrowdSystem();
        GroupId gid = crowd.createGroup(FormationType.WEDGE);
        crowd.addToGroup(gid, EntityId.of(10L), new Location(5, 0, 5));
        crowd.addToGroup(gid, EntityId.of(11L), new Location(6, 0, 5));
        crowd.tick(1L, 0.016f);
        assertEquals(2, crowd.latestSnapshot().totalAgents());
    }

    @Test
    void navigationOperatorsWireIntoTaskLibrary() {
        NavigationSystem nav = new DefaultNavigationSystem(
            NavMeshBuilder.buildGrid(4, 4, 2f, 2));
        EntityId npc = EntityId.of(1L);
        var task = org.dynamisai.planning.TaskLibrary.approachPlayerTask(
            nav, npc, new Location(6, 0, 6));
        assertNotNull(task);
        assertDoesNotThrow(() -> task.operator().run());
        ((DefaultNavigationSystem) nav).shutdown();
    }
}
