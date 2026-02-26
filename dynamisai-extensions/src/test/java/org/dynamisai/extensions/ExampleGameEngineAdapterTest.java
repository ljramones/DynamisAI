package org.dynamisai.extensions;

import org.dynamisai.core.AIOutputFrame;
import org.dynamisai.core.DynamisAiEngine;
import org.dynamisai.core.GameEngineAdapter;
import org.dynamisai.core.GameEngineContext;
import org.dynamisai.testkit.GameEngineAdapterContractTest;
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
