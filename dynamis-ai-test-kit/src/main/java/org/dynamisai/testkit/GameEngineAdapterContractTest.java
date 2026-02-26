package org.dynamisai.testkit;

import org.dynamisai.core.AIOutputFrame;
import org.dynamisai.core.DynamisAiEngine;
import org.dynamisai.core.GameEngineAdapter;
import org.dynamisai.core.GameEngineContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class GameEngineAdapterContractTest {

    protected abstract GameEngineAdapter createAdapter();

    protected DynamisAiEngine createEngine() {
        return DynamisAiEngine.builder().build();
    }

    @Test
    void adapterTicksAfterInitialize() {
        GameEngineAdapter adapter = createAdapter();
        adapter.initialize(createEngine());
        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f).build();
        AIOutputFrame frame = adapter.tick(ctx);
        assertNotNull(frame);
        assertEquals(1L, frame.tick());
    }

    @Test
    void adapterTickBeforeInitializeThrows() {
        GameEngineAdapter adapter = createAdapter();
        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f).build();
        assertThrows(Exception.class, () -> adapter.tick(ctx));
    }

    @Test
    void adapterShutdownAfterInitializeDoesNotThrow() {
        GameEngineAdapter adapter = createAdapter();
        adapter.initialize(createEngine());
        assertDoesNotThrow(adapter::shutdown);
    }

    @Test
    void adapterShutdownBeforeInitializeDoesNotThrow() {
        GameEngineAdapter adapter = createAdapter();
        assertDoesNotThrow(adapter::shutdown);
    }

    @Test
    void frameTickMatchesContextTick() {
        GameEngineAdapter adapter = createAdapter();
        adapter.initialize(createEngine());
        for (long t = 1; t <= 5; t++) {
            GameEngineContext ctx = GameEngineContext.builder(t, 0.016f).build();
            AIOutputFrame frame = adapter.tick(ctx);
            assertEquals(t, frame.tick());
        }
    }

    @Test
    void frameElapsedMsIsNonNegative() {
        GameEngineAdapter adapter = createAdapter();
        adapter.initialize(createEngine());
        GameEngineContext ctx = GameEngineContext.builder(1L, 0.016f).build();
        AIOutputFrame frame = adapter.tick(ctx);
        assertTrue(frame.elapsedMs() >= 0);
    }

    @Test
    void multipleTicksDoNotAccumulateStateAcrossFrames() {
        GameEngineAdapter adapter = createAdapter();
        adapter.initialize(createEngine());
        for (int i = 0; i < 10; i++) {
            GameEngineContext ctx = GameEngineContext.builder(i + 1L, 0.016f).build();
            AIOutputFrame frame = adapter.tick(ctx);
            assertNotNull(frame.worldSnapshot());
        }
    }
}
