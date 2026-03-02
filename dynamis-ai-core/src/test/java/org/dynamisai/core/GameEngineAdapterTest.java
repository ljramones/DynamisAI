package org.dynamisai.core;

import org.dynamis.core.entity.EntityId;
import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineAdapterTest {

    @Test
    void engineBuildsWithDefaults() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        assertNotNull(engine);
    }

    @Test
    void engineInitialisesOnce() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        assertDoesNotThrow(() -> {
            engine.initialize();
            engine.initialize();
        });
    }

    @Test
    void headlessAdapterTicksWithoutThrowing() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
        adapter.initialize(engine);

        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f)
            .observer(new Location(0, 0, 0))
            .build();

        AIOutputFrame frame = adapter.tick(ctx);
        assertNotNull(frame);
        assertEquals(1L, frame.tick());
    }

    @Test
    void tickBeforeInitializeThrows() {
        HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f).build();
        assertThrows(IllegalStateException.class, () -> adapter.tick(ctx));
    }

    @Test
    void entityPositionsCommittedToWorldStore() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
        adapter.initialize(engine);

        EntityId npc = EntityId.of(1L);
        Location pos = new Location(5, 0, 10);

        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f)
            .positions(java.util.Map.of(npc, pos))
            .build();

        adapter.tick(ctx);
        assertTrue(engine.currentSnapshot().entities().get(npc).isDefined());
    }

    @Test
    void emptyFrameReturnedWhenNoSystemsConfigured() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
        adapter.initialize(engine);

        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f).build();
        AIOutputFrame frame = adapter.tick(ctx);

        assertFalse(frame.hasDialogue());
        assertFalse(frame.hasSteering());
    }

    @Test
    void worldChangeAppliedViaContext() {
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
        adapter.initialize(engine);

        WorldChange change = new WorldChange.FactChange("weather", "rain");
        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f)
            .changes(java.util.List.of(change))
            .build();

        assertDoesNotThrow(() -> adapter.tick(ctx));
    }

    @Test
    void playerInputRecordRoundTrips() {
        EntityId player = EntityId.of(99L);
        EntityId target = EntityId.of(1L);
        PlayerInput input = PlayerInput.speech(player, target, "I come in peace");
        assertTrue(input.hasSpeech());
        assertTrue(input.hasTarget());
        assertEquals("I come in peace", input.speechText());
    }

    @Test
    void dialogueEventAudioReadyFlagFlips() {
        EntityId speaker = EntityId.of(1L);
        EntityId target = EntityId.of(2L);
        DialogueEvent event = DialogueEvent.of(speaker, target, "Halt!", "neutral");
        assertFalse(event.audioReady());
        assertTrue(event.withAudioReady().audioReady());
    }

    @Test
    void animationSignalIdleProducesNeutralAffect() {
        AnimationSignal sig = AnimationSignal.idle(EntityId.of(1L));
        assertEquals("neutral", sig.affectSummary());
        assertFalse(sig.isSpeaking());
    }

    @Test
    void steeringOutputStoppedIsNotAtGoal() {
        SteeringOutput out = SteeringOutput.stopped();
        assertFalse(out.isAtGoal());
        assertEquals(0f, out.speed(), 0.001f);
    }

    @Test
    void aiOutputFrameEmptyFactoryHasNoOutputs() {
        WorldSnapshot snap = new WorldSnapshot(
            1L,
            HashMap.empty(),
            new GlobalFacts(java.util.Map.of()),
            new EnvironmentState("clear", 12.0f, 1.0f),
            Long.hashCode(1L)
        );
        FrameBudgetReport budget = FrameBudgetReport.empty(1L);
        AIOutputFrame frame = AIOutputFrame.empty(1L, 5L, snap, budget);
        assertFalse(frame.hasDialogue());
        assertFalse(frame.hasSteering());
        assertTrue(frame.dialogueEvents().isEmpty());
    }
}
