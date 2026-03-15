package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.ai.cognition.DefaultCognitionService;
import org.dynamisengine.ai.cognition.DialogueRequest;
import org.dynamisengine.ai.cognition.DialogueResponse;
import org.dynamisengine.ai.cognition.MockInferenceBackend;
import org.dynamisengine.ai.core.AITaskNode;
import org.dynamisengine.ai.core.DefaultBudgetGovernor;
import org.dynamisengine.ai.core.DefaultWorldStateStore;
import org.dynamisengine.ai.core.DegradeMode;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.EntityState;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.Priority;
import org.dynamisengine.ai.core.QueryScope;
import org.dynamisengine.ai.core.ThreatLevel;
import org.dynamisengine.ai.core.WorldChange;
import org.dynamisengine.ai.core.WorldFacts;
import org.dynamisengine.ai.core.WorldSnapshot;
import org.dynamisengine.ai.crowd.CrowdSystem;
import org.dynamisengine.ai.crowd.DefaultCrowdSystem;
import org.dynamisengine.ai.crowd.FormationType;
import org.dynamisengine.ai.crowd.GroupId;
import org.dynamisengine.ai.memory.DefaultMemoryLifecycleManager;
import org.dynamisengine.ai.memory.InHeapVectorMemoryStore;
import org.dynamisengine.ai.memory.MemoryBudget;
import org.dynamisengine.ai.memory.MemoryRecord;
import org.dynamisengine.ai.memory.MemoryStats;
import org.dynamisengine.ai.navigation.DefaultNavigationSystem;
import org.dynamisengine.ai.navigation.NavMeshBuilder;
import org.dynamisengine.ai.navigation.NavigationSystem;
import org.dynamisengine.ai.perception.DefaultPerceptionSystem;
import org.dynamisengine.ai.perception.Percept;
import org.dynamisengine.ai.perception.PerceptionSnapshot;
import org.dynamisengine.ai.social.DefaultSocialSystem;
import org.dynamisengine.ai.social.SocialDialogueShaper;
import org.dynamisengine.ai.social.SocialSystem;
import org.dynamisengine.ai.voice.DefaultAnimisBridge;
import org.dynamisengine.ai.voice.IntentSignal;
import org.dynamisengine.ai.voice.IntentType;
import org.dynamisengine.ai.voice.MockTTSPipeline;
import org.dynamisengine.ai.voice.PhysicalVoiceContext;
import org.dynamisengine.ai.voice.VoiceRenderJob;
import org.dynamisengine.core.logging.DynamisLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Headless single-NPC demo reel.
 *
 * Wires: WorldStateStore -> BudgetGovernor -> PerceptionSystem
 *        -> CognitionService -> TTSPipeline -> AnimisBridge
 *        -> MemoryLifecycleManager
 */
public final class SingleNpcDemoReel {

    private static final DynamisLogger log = DynamisLogger.get(SingleNpcDemoReel.class);

    private final int tickCount;
    private final String[] playerInputs;

    private final NavigationSystem navigationSystem;
    private final SocialSystem socialSystem;
    private final CrowdSystem crowdSystem;

    private DefaultWorldStateStore worldStateStore;
    private EntityId npcId;
    private long currentTick;

    public SingleNpcDemoReel(int tickCount, String[] playerInputs) {
        this.tickCount = tickCount;
        this.playerInputs = playerInputs;
        this.navigationSystem = new DefaultNavigationSystem(
            NavMeshBuilder.buildGrid(8, 8, 2f, 4));
        this.socialSystem = new DefaultSocialSystem();
        this.crowdSystem = new DefaultCrowdSystem();
    }

    public DemoReelReport run() throws Exception {
        EntityId npc = EntityId.of(1L);
        EntityId player = EntityId.of(2L);
        this.npcId = npc;

        DefaultWorldStateStore worldStore = new DefaultWorldStateStore();
        this.worldStateStore = worldStore;

        DefaultBudgetGovernor governor = new DefaultBudgetGovernor(16);
        MockInferenceBackend backend = new MockInferenceBackend();
        DefaultCognitionService cognition = new DefaultCognitionService(backend);
        MockTTSPipeline tts = new MockTTSPipeline();
        DefaultAnimisBridge animis = new DefaultAnimisBridge();
        DefaultMemoryLifecycleManager memory =
            new DefaultMemoryLifecycleManager(MemoryBudget.tier1(),
                new InHeapVectorMemoryStore());
        DefaultPerceptionSystem perception = new DefaultPerceptionSystem();

        worldStore.enqueueChange(new WorldChange.EntityStateChange(npc,
            new EntityState(npc, new Location(0, 0, 0), Map.of())));
        worldStore.enqueueChange(new WorldChange.EntityStateChange(player,
            new EntityState(player, new Location(5, 0, 0), Map.of())));
        worldStore.commitTick();

        GroupId crowdGroup = crowdSystem.createGroup(FormationType.LINE);
        crowdSystem.addToGroup(crowdGroup, npc, new Location(0, 0, 0));
        crowdSystem.addToGroup(crowdGroup, player, new Location(5, 0, 0));
        crowdSystem.setGroupGoal(crowdGroup, new Location(10, 0, 10));

        governor.register(new AITaskNode(
            "perception", 2, Priority.HIGH, DegradeMode.CACHED,
            () -> perception.tick(npc, AffectVector.neutral(), worldStore),
            () -> log.debug("Perception fallback")
        ));

        governor.register(new AITaskNode(
            "navigation-steer", 2, Priority.HIGH, DegradeMode.CACHED,
            () -> navigationSystem.steer(npcId, currentPosition(), 4f),
            () -> log.debug("Navigation steer fallback")
        ));

        governor.register(new AITaskNode(
            "memory-consolidate", 1, Priority.NORMAL, DegradeMode.DEFER,
            () -> memory.consolidate(npc),
            () -> log.debug("Memory consolidation deferred")
        ));

        governor.register(new AITaskNode(
            "crowd-tick", 3, Priority.NORMAL, DegradeMode.DEFER,
            () -> crowdSystem.tick(currentTick, 0.016f),
            () -> log.debug("Crowd tick deferred")
        ));

        List<DemoTickResult> results = new ArrayList<>();
        int successDialogue = 0;
        int fallbackDialogue = 0;
        int totalPercepts = 0;
        int totalMemory = 0;

        for (int i = 0; i < tickCount; i++) {
            long frameStart = System.nanoTime();

            if (i % 2 == 0) {
                worldStore.enqueueChange(new WorldChange.FactChange("threatLevel", ThreatLevel.LOW));
            } else {
                worldStore.enqueueChange(new WorldChange.FactChange("threatLevel", ThreatLevel.NONE));
            }
            worldStore.commitTick();
            long tick = worldStore.getCurrentTick();
            this.currentTick = tick;

            WorldSnapshot snap = worldStore.getCurrentSnapshot();
            governor.runFrame(tick, snap);

            PerceptionSnapshot percepts = perception.getLastSnapshot(npc);
            totalPercepts += percepts.percepts().size();

            String input = playerInputs[i % playerInputs.length];
            DialogueRequest request = new DialogueRequest(
                npc, player, input,
                new WorldFacts(Map.of(), percepts.percepts().stream()
                    .map(Percept::source).toList(),
                    percepts.aggregateThreat(),
                    percepts.ownerLocation(), null),
                AffectVector.neutral(), snap
            );

            DialogueRequest shaped = SocialDialogueShaper.shape(request, socialSystem, npc, player);
            DialogueResponse dialogue = cognition.requestDialogue(shaped).get(2, TimeUnit.SECONDS);

            boolean wasFallback = "...".equals(dialogue.text());
            if (wasFallback) {
                fallbackDialogue++;
            } else {
                successDialogue++;
            }

            socialSystem.recordDialogue(npc, player, dialogue.text(), "demo", dialogue.affect().valence(), tick);

            MemoryRecord memEvent = MemoryRecord.rawEvent(npc,
                "Spoke to player: " + input.substring(0, Math.min(20, input.length())),
                dialogue.text(), 0.6f);
            memory.addRawEvent(memEvent);
            totalMemory++;

            VoiceRenderJob voiceJob = tts.render(dialogue,
                PhysicalVoiceContext.calm(), npc).get(2, TimeUnit.SECONDS);
            animis.submitVoiceJob(voiceJob);
            animis.submitIntentSignal(npc,
                IntentSignal.certain(IntentType.APPROACH_FRIENDLY),
                Duration.ofMillis(180));
            animis.pushAffectState(npc, dialogue.affect());
            animis.pollEvents(npc);

            long frameBudgetMs = (System.nanoTime() - frameStart) / 1_000_000;
            MemoryStats stats = memory.getStats(npc);

            results.add(new DemoTickResult(
                tick,
                percepts.percepts().size(),
                percepts.aggregateThreat(),
                dialogue.text(),
                dialogue.fromCache(),
                stats,
                frameBudgetMs
            ));

            log.info(String.format("Tick %s - percepts=%s threat=%s dialogue='%s' memTotal=%s", tick, percepts.percepts().size(), percepts.aggregateThreat(),
                dialogue.text().substring(0, Math.min(30, dialogue.text().length())),
                stats.totalCount()));
        }

        cognition.shutdown();
        if (navigationSystem instanceof DefaultNavigationSystem nav) {
            nav.shutdown();
        }

        return new DemoReelReport(
            "npc-" + npc.id(),
            tickCount,
            successDialogue,
            fallbackDialogue,
            totalPercepts,
            totalMemory,
            results
        );
    }

    private Location currentPosition() {
        if (worldStateStore == null || npcId == null) {
            return new Location(0, 0, 0);
        }
        return worldStateStore.query(npcId, QueryScope.tactical(new Location(0, 0, 0)))
            .agentPosition();
    }
}
