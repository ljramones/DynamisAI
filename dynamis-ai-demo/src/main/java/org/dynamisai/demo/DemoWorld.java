package org.dynamisai.demo;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DefaultCognitionService;
import org.dynamisai.cognition.DialogueRequest;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamisai.cognition.MockInferenceBackend;
import org.dynamisai.core.AITaskNode;
import org.dynamisai.core.DefaultBudgetGovernor;
import org.dynamisai.core.DefaultWorldStateStore;
import org.dynamisai.core.DegradeMode;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.Priority;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldChange;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.crowd.CrowdSnapshot;
import org.dynamisai.crowd.DefaultCrowdSystem;
import org.dynamisai.crowd.FormationType;
import org.dynamisai.crowd.GroupId;
import org.dynamisai.memory.DefaultMemoryLifecycleManager;
import org.dynamisai.memory.MemoryBudget;
import org.dynamisai.memory.MemoryRecord;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.navigation.DefaultNavigationSystem;
import org.dynamisai.navigation.NavMesh;
import org.dynamisai.navigation.NavMeshBuilder;
import org.dynamisai.navigation.PathRequest;
import org.dynamisai.navigation.SteeringOutput;
import org.dynamisai.perception.DefaultPerceptionSystem;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.social.DefaultSocialSystem;
import org.dynamisai.social.Relationship;
import org.dynamisai.social.RelationshipTag;
import org.dynamisai.social.SocialDialogueShaper;
import org.dynamisai.voice.DefaultAnimisBridge;
import org.dynamisai.voice.MockTTSPipeline;
import org.dynamisai.voice.PhysicalVoiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Owns all DynamisAI system instances and orchestrates the demo tick loop.
 */
public final class DemoWorld {

    private static final Logger log = LoggerFactory.getLogger(DemoWorld.class);

    private static final float DIALOGUE_RANGE = 8f;
    private static final float PERCEPTION_RANGE = 12f;
    private static final int HOSTILE_THRESHOLD = 3;

    private final DefaultWorldStateStore worldStore;
    private final DefaultBudgetGovernor governor;
    private final DefaultPerceptionSystem perception;
    private final DefaultCognitionService cognition;
    private final DefaultMemoryLifecycleManager memory;
    private final DefaultNavigationSystem navigation;
    final DefaultSocialSystem social;
    final DefaultCrowdSystem crowd;
    private final MockTTSPipeline tts;
    private final DefaultAnimisBridge animisBridge;

    final DemoNpc guard1;
    final DemoNpc guard2;
    final DemoNpc player;

    final GroupId patrolGroup;
    private final DemoReport report;
    private final AtomicLong currentTick = new AtomicLong(0L);

    private static final Location[] WAYPOINTS = {
        new Location(2, 0, 2),
        new Location(14, 0, 2),
        new Location(14, 0, 14),
        new Location(2, 0, 14)
    };
    private int waypointIndex = 0;

    private int hostileCount = 0;
    private String outcome = "unresolved";

    public DemoWorld() {
        guard1 = new DemoNpc(EntityId.of(1L), "Guard1", new Location(2, 0, 2));
        guard2 = new DemoNpc(EntityId.of(2L), "Guard2", new Location(4, 0, 2));
        player = new DemoNpc(EntityId.of(3L), "Player", new Location(28, 0, 28));

        report = new DemoReport();

        worldStore = new DefaultWorldStateStore();
        governor = new DefaultBudgetGovernor(16);

        NavMesh mesh = NavMeshBuilder.buildGrid(16, 16, 2f, 4);
        navigation = new DefaultNavigationSystem(mesh);

        perception = new DefaultPerceptionSystem();
        cognition = new DefaultCognitionService(new MockInferenceBackend());
        memory = new DefaultMemoryLifecycleManager(MemoryBudget.tier1(), new DemoVectorMemoryStore());
        social = new DefaultSocialSystem();

        crowd = new DefaultCrowdSystem();
        patrolGroup = crowd.createGroup(FormationType.LINE);
        crowd.addToGroup(patrolGroup, guard1.id, guard1.position);
        crowd.addToGroup(patrolGroup, guard2.id, guard2.position);
        crowd.setGroupGoal(patrolGroup, WAYPOINTS[0]);

        tts = new MockTTSPipeline();
        animisBridge = new DefaultAnimisBridge();

        governor.register(new AITaskNode(
            "crowd-tick", 3, Priority.NORMAL, DegradeMode.DEFER,
            () -> crowd.tick(currentTick.get(), 0.016f),
            () -> {
            }
        ));
        governor.register(new AITaskNode(
            "memory-consolidate", 1, Priority.NORMAL, DegradeMode.DEFER,
            () -> {
                memory.consolidate(guard1.id);
                memory.consolidate(guard2.id);
            },
            () -> {
            }
        ));
        governor.register(new AITaskNode(
            "navigation-steer", 2, Priority.HIGH, DegradeMode.CACHED,
            () -> {
                navigation.steer(guard1.id, guard1.position, 4f);
                navigation.steer(guard2.id, guard2.position, 4f);
            },
            () -> {
            }
        ));

        pushWorldState();

        log.info("DemoWorld initialized - 3 entities, NavMesh 16x16, {} clusters", mesh.clusterCount());
    }

    public TickRecord tick(long tickNum, PlayerAction action, String playerSpeech) {
        currentTick.set(tickNum);
        List<String> systems = new ArrayList<>();

        applyPlayerAction(action, playerSpeech);
        systems.add("PlayerInput");

        pushWorldState();
        systems.add("WorldStateStore");

        advancePatrol();

        governor.runFrame(tickNum, worldStore.getCurrentSnapshot());
        systems.add("BudgetGovernor");
        systems.add("CrowdSystem");

        PerceptionSnapshot guardPerception = perception.tick(guard1.id, guard1.affect, worldStore);
        float distG1ToPlayer = guard1.distanceTo(player.position);
        boolean playerVisible = distG1ToPlayer < PERCEPTION_RANGE;
        String perceptionLine;
        if (playerVisible) {
            float salience = Math.max(0f, 1f - distG1ToPlayer / PERCEPTION_RANGE);
            perceptionLine = String.format("%s sees Player (dist=%.1fm, salience=%.2f, threat=%s)",
                guard1.name, distG1ToPlayer, salience, guard1.perceivedThreat);
            guard1.isAlert = true;
            guard2.isAlert = true;
            systems.add("PerceptionSystem");
        } else {
            perceptionLine = guard1.name + " cannot see Player";
            guard1.isAlert = false;
            guard2.isAlert = false;
        }

        String g1Task = deriveTask(guard1, distG1ToPlayer, playerVisible);
        String g2Task = guard2.isAlert ? "callBackup" : "patrol";
        guard1.currentTask = g1Task;
        guard2.currentTask = g2Task;
        systems.add("HtnPlanner");

        if ("approachPlayer".equals(g1Task)) {
            navigation.requestPath(PathRequest.of(guard1.id, guard1.position, player.position));
            systems.add("NavigationSystem");
        } else if ("fleeThreat".equals(g1Task) || "callBackup".equals(g1Task)) {
            Location flee = new Location(guard1.position.x() - 6, guard1.position.y(), guard1.position.z() - 6);
            navigation.requestPath(PathRequest.of(guard1.id, guard1.position, flee));
            systems.add("NavigationSystem");
        }

        String guard1Speech = "";
        String guard2Speech = "";
        if (playerVisible && distG1ToPlayer < DIALOGUE_RANGE) {
            String contextInput = playerSpeech.isEmpty()
                ? buildContextInput(g1Task, action)
                : "Player said: " + playerSpeech;

            DialogueRequest raw = new DialogueRequest(
                guard1.id,
                player.id,
                contextInput,
                new WorldFacts(Map.of(), guardPerception.percepts().stream().map(p -> p.source()).toList(),
                    guardPerception.aggregateThreat(), guardPerception.ownerLocation(), null),
                guard1.affect,
                worldStore.getCurrentSnapshot()
            );

            DialogueRequest shaped = SocialDialogueShaper.shape(raw, social);
            systems.add("SocialDialogueShaper");

            try {
                DialogueResponse response = cognition.requestDialogue(shaped).get(2, TimeUnit.SECONDS);
                guard1Speech = response.text();
                guard1.affect = response.affect();

                social.recordDialogue(guard1.id, player.id,
                    guard1Speech, g1Task, guard1.affect.valence(), tickNum);
                systems.add("CognitionService");
                systems.add("SocialSystem");

                memory.addRawEvent(MemoryRecord.rawEvent(
                    guard1.id,
                    "Spoke to Player at tick " + tickNum + ": " + g1Task,
                    guard1Speech,
                    computeMemoryImportance(g1Task)));
                systems.add("MemoryLifecycleManager");

                tts.render(response, PhysicalVoiceContext.calm(), guard1.id)
                    .thenAccept(animisBridge::submitVoiceJob)
                    .join();
                systems.add("TTSPipeline");
                systems.add("AnimisBridge");

            } catch (Exception e) {
                guard1Speech = "(no response)";
            }
        }

        Relationship rel = social.graph().get(guard1.id, player.id);
        String socialLine = describeSocial(rel);

        MemoryStats mStats = memory.getStats(guard1.id);
        String memoryLine = playerVisible
            ? "Guard1 memories total=" + mStats.totalCount()
            : "no new memory event";

        CrowdSnapshot snap = crowd.latestSnapshot();
        String crowdLod = snap.groups().containsKey(patrolGroup)
            ? snap.groups().get(patrolGroup).lod().name()
            : "UNKNOWN";

        syncPositionsFromSnapshot(snap);
        checkOutcome(tickNum, distG1ToPlayer);

        TickRecord record = new TickRecord(
            tickNum,
            action.name(),
            playerSpeech,
            guard1Speech,
            guard2Speech,
            g1Task,
            g2Task,
            crowdLod,
            socialLine,
            memoryLine,
            perceptionLine,
            systems
        );

        report.add(record);
        return record;
    }

    public DemoReport report() {
        return report;
    }

    public String outcome() {
        return outcome;
    }

    public boolean isResolved() {
        return !outcome.equals("unresolved");
    }

    public void shutdown() {
        cognition.shutdown();
        navigation.shutdown();
    }

    private void applyPlayerAction(PlayerAction action, String speech) {
        Location pos = player.position;
        float dx = (float) (guard1.position.x() - pos.x());
        float dz = (float) (guard1.position.z() - pos.z());
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        switch (action) {
            case APPROACH -> {
                if (dist > 0.5f) {
                    float step = Math.min(3f, dist);
                    player.position = new Location(
                        pos.x() + (dx / dist) * step,
                        pos.y(),
                        pos.z() + (dz / dist) * step
                    );
                }
            }
            case FLEE -> {
                float step = 5f;
                player.position = dist > 0.01f
                    ? new Location(pos.x() - (dx / dist) * step, pos.y(), pos.z() - (dz / dist) * step)
                    : new Location(pos.x() + step, pos.y(), pos.z());

                if (guard1.distanceTo(player.position) > 25f) {
                    outcome = "Player escaped";
                }
            }
            case HOSTILE -> {
                hostileCount++;
                social.tagRelationshipBoth(guard1.id, player.id, RelationshipTag.ENEMY);
                social.adjustTrust(guard1.id, player.id, -0.4f);
                guard1.perceivedThreat = ThreatLevel.HIGH;
                guard2.perceivedThreat = ThreatLevel.HIGH;
                guard2.isAlert = true;

                if (hostileCount >= HOSTILE_THRESHOLD) {
                    social.tagRelationshipBoth(guard1.id, player.id, RelationshipTag.BETRAYED);
                    outcome = "Guards hostile - combat imminent";
                }
            }
            case WAIT, SPEAK -> {
                // no movement
            }
        }
    }

    private void advancePatrol() {
        float dist = guard1.distanceTo(WAYPOINTS[waypointIndex]);
        if (dist < 3f) {
            waypointIndex = (waypointIndex + 1) % WAYPOINTS.length;
            crowd.setGroupGoal(patrolGroup, WAYPOINTS[waypointIndex]);
        }
    }

    private String deriveTask(DemoNpc guard, float distToPlayer, boolean playerVisible) {
        Relationship rel = social.graph().get(guard.id, player.id);
        if (rel.hasTag(RelationshipTag.ENEMY) || rel.hasTag(RelationshipTag.BETRAYED)) {
            return distToPlayer < 5f ? "callBackup" : "fleeThreat";
        }
        if (!playerVisible) return "patrol";
        if (distToPlayer < DIALOGUE_RANGE) return "initiateDialogue";
        return "approachPlayer";
    }

    private String buildContextInput(String task, PlayerAction action) {
        return switch (task) {
            case "initiateDialogue" -> "A stranger approaches. State your business.";
            case "fleeThreat" -> "Threat detected! Calling for help!";
            case "callBackup" -> "Sound the alarm!";
            case "approachPlayer" -> "Who goes there?";
            default -> action == PlayerAction.WAIT ? "All clear." : "Stay where you are.";
        };
    }

    private float computeMemoryImportance(String task) {
        return switch (task) {
            case "callBackup", "fleeThreat" -> 0.9f;
            case "initiateDialogue" -> 0.6f;
            default -> 0.3f;
        };
    }

    private String describeSocial(Relationship rel) {
        StringBuilder sb = new StringBuilder();
        sb.append("Guard1<->Player: ");
        if (rel.tags().isEmpty() || rel.tags().contains(RelationshipTag.NEUTRAL)) {
            sb.append("neutral");
        } else {
            rel.tags().forEach(t -> sb.append(t.name().toLowerCase()).append(" "));
        }
        sb.append(String.format("(trust=%.2f affinity=%.2f interactions=%d)",
            rel.trust(), rel.affinity(), rel.interactionCount()));
        return sb.toString().trim();
    }

    private void syncPositionsFromSnapshot(CrowdSnapshot snap) {
        if (!snap.groups().containsKey(patrolGroup)) {
            return;
        }
        for (CrowdSnapshot.AgentSnapshot a : snap.groups().get(patrolGroup).agents()) {
            if (a.id().equals(guard1.id)) {
                guard1.position = new Location(a.position().x(), a.position().y(), a.position().z());
            } else if (a.id().equals(guard2.id)) {
                guard2.position = new Location(a.position().x(), a.position().y(), a.position().z());
            }
        }
    }

    private void checkOutcome(long tick, float distToPlayer) {
        if (!outcome.equals("unresolved")) {
            return;
        }
        Relationship rel = social.graph().get(guard1.id, player.id);
        if (rel.trust() > 0.5f && rel.interactionCount() >= 3) {
            outcome = "Alliance formed - guards trust the player";
        } else if (tick >= 20 && distToPlayer > DIALOGUE_RANGE) {
            outcome = "Patrol continued - encounter unresolved";
        }
    }

    private void pushWorldState() {
        worldStore.enqueueChange(new WorldChange.EntityStateChange(
            guard1.id, new EntityState(guard1.id, guard1.position, Map.of("name", guard1.name))));
        worldStore.enqueueChange(new WorldChange.EntityStateChange(
            guard2.id, new EntityState(guard2.id, guard2.position, Map.of("name", guard2.name))));
        worldStore.enqueueChange(new WorldChange.EntityStateChange(
            player.id, new EntityState(player.id, player.position, Map.of("name", player.name))));
        worldStore.enqueueChange(new WorldChange.FactChange("threatLevel", guard1.perceivedThreat));
        worldStore.commitTick();
    }
}
