package org.dynamisai.tools;

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
import org.dynamisai.core.QueryScope;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldChange;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.core.WorldSnapshot;
import org.dynamisai.crowd.CrowdSystem;
import org.dynamisai.crowd.DefaultCrowdSystem;
import org.dynamisai.crowd.FormationType;
import org.dynamisai.crowd.GroupId;
import org.dynamisai.memory.DefaultMemoryLifecycleManager;
import org.dynamisai.memory.InHeapVectorMemoryStore;
import org.dynamisai.memory.MemoryBudget;
import org.dynamisai.memory.MemoryRecord;
import org.dynamisai.memory.MemoryStats;
import org.dynamisai.navigation.DefaultNavigationSystem;
import org.dynamisai.navigation.NavMeshBuilder;
import org.dynamisai.navigation.NavigationSystem;
import org.dynamisai.perception.DefaultPerceptionSystem;
import org.dynamisai.perception.Percept;
import org.dynamisai.perception.PerceptionSnapshot;
import org.dynamisai.social.DefaultSocialSystem;
import org.dynamisai.social.SocialDialogueShaper;
import org.dynamisai.social.SocialSystem;
import org.dynamisai.voice.DefaultAnimisBridge;
import org.dynamisai.voice.IntentSignal;
import org.dynamisai.voice.IntentType;
import org.dynamisai.voice.MockTTSPipeline;
import org.dynamisai.voice.PhysicalVoiceContext;
import org.dynamisai.voice.VoiceRenderJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(SingleNpcDemoReel.class);

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

            log.info("Tick {} - percepts={} threat={} dialogue='{}' memTotal={}",
                tick, percepts.percepts().size(), percepts.aggregateThreat(),
                dialogue.text().substring(0, Math.min(30, dialogue.text().length())),
                stats.totalCount());
        }

        cognition.shutdown();
        if (navigationSystem instanceof DefaultNavigationSystem nav) {
            nav.shutdown();
        }

        return new DemoReelReport(
            "npc-" + npc.value(),
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
