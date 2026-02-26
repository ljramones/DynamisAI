package org.dynamisai.crowd;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.navigation.NavPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CrowdSystemTest {

    private DefaultCrowdSystem crowd;
    private EntityId a, b, c;

    @BeforeEach
    void setUp() {
        crowd = new DefaultCrowdSystem();
        a = EntityId.of(1L);
        b = EntityId.of(2L);
        c = EntityId.of(3L);
    }

    // ── CrowdLod ──────────────────────────────────────────────────────────────

    @Test
    void lodFullForNearGroup() {
        assertEquals(CrowdLod.FULL, CrowdLod.forDistance(10f));
    }

    @Test
    void lodReducedForMidRange() {
        assertEquals(CrowdLod.REDUCED, CrowdLod.forDistance(40f));
    }

    @Test
    void lodSkeletonForFarGroup() {
        assertEquals(CrowdLod.SKELETON, CrowdLod.forDistance(100f));
    }

    @Test
    void lodCulledForDistantGroup() {
        assertEquals(CrowdLod.CULLED, CrowdLod.forDistance(200f));
    }

    // ── Formation ─────────────────────────────────────────────────────────────

    @Test
    void lineFormationProducesCorrectSlotCount() {
        List<FormationSlot> slots = Formation.compute(
            FormationType.LINE,
            NavPoint.of(0, 0, 0),
            NavPoint.of(0, 0, 1),
            4);
        assertEquals(4, slots.size());
    }

    @Test
    void columnFormationSlotsAreCollinear() {
        NavPoint facing = NavPoint.of(0, 0, 1);
        List<FormationSlot> slots = Formation.compute(
            FormationType.COLUMN,
            NavPoint.of(0, 0, 0), facing, 3);
        float x0 = slots.get(0).worldPosition().x();
        for (FormationSlot s : slots) {
            assertEquals(x0, s.worldPosition().x(), 0.01f);
        }
    }

    @Test
    void wedgeLeaderIsAtFront() {
        NavPoint facing = NavPoint.of(0, 0, 1);
        List<FormationSlot> slots = Formation.compute(
            FormationType.WEDGE,
            NavPoint.of(0, 0, 0), facing, 5);
        float leaderZ = slots.get(0).worldPosition().z();
        for (int i = 1; i < slots.size(); i++) {
            assertTrue(leaderZ >= slots.get(i).worldPosition().z(),
                "Leader must be at front of wedge");
        }
    }

    @Test
    void circleFormationAgentsEquallySpaced() {
        NavPoint centroid = NavPoint.of(0, 0, 0);
        List<FormationSlot> slots = Formation.compute(
            FormationType.CIRCLE, centroid,
            NavPoint.of(0, 0, 1), 6);
        assertEquals(6, slots.size());
        float r0 = centroid.distanceTo(slots.get(0).worldPosition());
        for (FormationSlot s : slots) {
            assertEquals(r0, centroid.distanceTo(s.worldPosition()), 0.01f);
        }
    }

    @Test
    void scatterFormationIsDeteministic() {
        NavPoint centroid = NavPoint.of(5, 0, 5);
        NavPoint facing = NavPoint.of(1, 0, 0);
        List<FormationSlot> first = Formation.compute(
            FormationType.SCATTER, centroid, facing, 4);
        List<FormationSlot> second = Formation.compute(
            FormationType.SCATTER, centroid, facing, 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(first.get(i).worldPosition().x(),
                second.get(i).worldPosition().x(), 0.001f);
            assertEquals(first.get(i).worldPosition().z(),
                second.get(i).worldPosition().z(), 0.001f);
        }
    }

    @Test
    void emptyAgentCountReturnsEmptySlots() {
        List<FormationSlot> slots = Formation.compute(
            FormationType.LINE, NavPoint.of(0, 0, 0),
            NavPoint.of(0, 0, 1), 0);
        assertTrue(slots.isEmpty());
    }

    // ── CrowdGroup ────────────────────────────────────────────────────────────

    @Test
    void addAgentSetsSlotIndex() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.addToGroup(gid, b, new Location(1, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        var groupSnap = snap.groups().get(gid);
        assertNotNull(groupSnap);
        assertEquals(2, groupSnap.agents().size());
        assertEquals(0, groupSnap.agents().get(0).slotIndex());
        assertEquals(1, groupSnap.agents().get(1).slotIndex());
    }

    @Test
    void firstAgentIsLeader() {
        GroupId gid = crowd.createGroup(FormationType.COLUMN);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.addToGroup(gid, b, new Location(1, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        var agentA = snap.findAgent(a);
        assertTrue(agentA.isPresent());
        assertTrue(agentA.get().isLeader());
    }

    @Test
    void removeAgentPromotsNewLeader() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.addToGroup(gid, b, new Location(1, 0, 0));
        crowd.removeFromGroup(a);
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        var agentB = snap.findAgent(b);
        assertTrue(agentB.isPresent());
        assertTrue(agentB.get().isLeader(),
            "After leader removal, next agent must become leader");
    }

    @Test
    void groupOfReturnsCorrectGroup() {
        GroupId gid = crowd.createGroup(FormationType.WEDGE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        Optional<GroupId> found = crowd.groupOf(a);
        assertTrue(found.isPresent());
        assertEquals(gid, found.get());
    }

    @Test
    void dissolveGroupRemovesAllAgents() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.addToGroup(gid, b, new Location(1, 0, 0));
        crowd.dissolveGroup(gid);
        assertTrue(crowd.groupOf(a).isEmpty());
        assertTrue(crowd.groupOf(b).isEmpty());
        assertEquals(0, crowd.totalAgents());
    }

    // ── Tick / Movement ───────────────────────────────────────────────────────

    @Test
    void tickProducesSnapshot() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertNotNull(snap);
        assertEquals(1L, snap.tick());
    }

    @Test
    void agentMovesTowardFormationSlot() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.addToGroup(gid, b, new Location(100, 0, 0));
        crowd.setGroupGoal(gid, new Location(50, 0, 50));

        CrowdSnapshot before = crowd.tick(1L, 0.016f);
        NavPoint posABefore = before.findAgent(a).get().position();

        crowd.tick(2L, 0.016f);
        crowd.tick(3L, 0.016f);
        CrowdSnapshot after = crowd.tick(4L, 0.016f);
        NavPoint posAAfter = after.findAgent(a).get().position();

        float moved = posABefore.distanceTo(posAAfter);
        assertTrue(moved > 0f, "Agent must move toward formation slot over time");
    }

    @Test
    void latestSnapshotUpdatesEachTick() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.tick(1L, 0.016f);
        crowd.tick(2L, 0.016f);
        assertEquals(2L, crowd.latestSnapshot().tick());
    }

    @Test
    void emptyGroupProducesEmptySnapshot() {
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertEquals(0, snap.totalAgents());
    }

    // ── LOD integration ───────────────────────────────────────────────────────

    @Test
    void groupIsCulledWhenFarFromObserver() {
        crowd.setObserver(new Location(0, 0, 0));
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(200, 0, 200));
        crowd.addToGroup(gid, b, new Location(201, 0, 200));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        var groupSnap = snap.groups().get(gid);
        assertNotNull(groupSnap);
        assertEquals(CrowdLod.CULLED, groupSnap.lod());
    }

    @Test
    void groupIsFullLodWhenNearObserver() {
        crowd.setObserver(new Location(0, 0, 0));
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(1, 0, 0));
        crowd.addToGroup(gid, b, new Location(2, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        var groupSnap = snap.groups().get(gid);
        assertEquals(CrowdLod.FULL, groupSnap.lod());
    }

    // ── CrowdSnapshot ─────────────────────────────────────────────────────────

    @Test
    void snapshotGroupOfFindsCorrectGroup() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        Optional<CrowdSnapshot.GroupSnapshot> found = snap.groupOf(a);
        assertTrue(found.isPresent());
        assertEquals(gid, found.get().id());
    }

    @Test
    void snapshotTotalAgentsIsCorrect() {
        GroupId g1 = crowd.createGroup(FormationType.LINE);
        GroupId g2 = crowd.createGroup(FormationType.COLUMN);
        crowd.addToGroup(g1, a, new Location(0, 0, 0));
        crowd.addToGroup(g1, b, new Location(1, 0, 0));
        crowd.addToGroup(g2, c, new Location(10, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertEquals(3, snap.totalAgents());
    }

    @Test
    void crowdAgentRejectsNegativeSeparationRadius() {
        assertThrows(IllegalArgumentException.class, () ->
            new CrowdAgent(a, NavPoint.of(0, 0, 0), NavPoint.of(0, 0, 0),
                0, false, -1f));
    }

    @Test
    void setFormationChangesFormationType() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.setFormation(gid, FormationType.WEDGE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertEquals(FormationType.WEDGE, snap.groups().get(gid).formation());
    }

    @Test
    void groupIdNextProducesUniqueIds() {
        GroupId g1 = GroupId.next();
        GroupId g2 = GroupId.next();
        assertNotEquals(g1, g2);
    }

    @Test
    void lodControllerUsesObserverDistance() {
        LodController lod = new LodController();
        CrowdGroup group = new CrowdGroup(GroupId.of(999), FormationType.LINE);
        group.addAgent(CrowdAgent.of(a, NavPoint.of(100, 0, 0)));
        group.recomputeCentroid();
        lod.setObserver(NavPoint.of(0, 0, 0));
        assertEquals(CrowdLod.SKELETON, lod.assignLod(group));
    }

    @Test
    void crowdSnapshotEmptyFactoryWorks() {
        CrowdSnapshot empty = CrowdSnapshot.empty(7L);
        assertEquals(7L, empty.tick());
        assertEquals(0, empty.totalAgents());
    }

    @Test
    void findAgentReturnsEmptyWhenMissing() {
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertTrue(snap.findAgent(EntityId.of(999L)).isEmpty());
    }

    @Test
    void setGroupGoalUpdatesSnapshotGoal() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        crowd.setGroupGoal(gid, new Location(9, 0, 9));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertEquals(9f, snap.groups().get(gid).goal().x(), 0.001f);
        assertEquals(9f, snap.groups().get(gid).goal().z(), 0.001f);
    }

    @Test
    void snapshotCollectionsAreUnmodifiable() {
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(0, 0, 0));
        CrowdSnapshot snap = crowd.tick(1L, 0.016f);
        assertThrows(UnsupportedOperationException.class,
            () -> snap.groups().clear());
        assertThrows(UnsupportedOperationException.class,
            () -> snap.groups().get(gid).agents().add(null));
    }

    @Test
    void culledGroupHoldsAgentPositionAcrossTicks() {
        crowd.setObserver(new Location(0, 0, 0));
        GroupId gid = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(gid, a, new Location(300, 0, 300));
        CrowdSnapshot s1 = crowd.tick(1L, 0.016f);
        NavPoint p1 = s1.findAgent(a).orElseThrow().position();
        CrowdSnapshot s2 = crowd.tick(2L, 0.016f);
        NavPoint p2 = s2.findAgent(a).orElseThrow().position();
        assertEquals(CrowdLod.CULLED, s2.groups().get(gid).lod());
        assertEquals(p1.x(), p2.x(), 0.0001f);
        assertEquals(p1.z(), p2.z(), 0.0001f);
    }
}
