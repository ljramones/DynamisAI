package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.core.AIOutputFrame;
import org.dynamisengine.ai.core.DynamisAiEngine;
import org.dynamisengine.ai.core.GameEngineAdapter;
import org.dynamisengine.ai.core.GameEngineContext;
import org.dynamisengine.ai.testkit.GameEngineAdapterContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleGameEngineAdapterTest extends GameEngineAdapterContractTest {

    @Override
    protected GameEngineAdapter createAdapter() {
        return new ExampleGameEngineAdapter();
    }

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleGameEngineAdapter adapter = new ExampleGameEngineAdapter();
        DynamisAiEngine engine = DynamisAiEngine.builder().build();
        adapter.initialize(engine);
        AIOutputFrame frame = adapter.tick(GameEngineContext.builder(1L, 0.016f).build());
        assertNotNull(frame);
        adapter.shutdown();
    }
}
