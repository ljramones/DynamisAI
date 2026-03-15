package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.core.AITaskNode;
import org.dynamisengine.ai.core.AbstractGameEngineAdapter;
import org.dynamisengine.ai.core.DegradeMode;
import org.dynamisengine.ai.core.DynamisAiEngine;
import org.dynamisengine.ai.core.Priority;

/**
 * Minimal GameEngineAdapter SPI example.
 *
 * A real adapter would bridge a concrete engine (libGDX, jME, custom) by
 * mapping scene entities into GameEngineContext and applying AIOutputFrame.
 */
public final class ExampleGameEngineAdapter extends AbstractGameEngineAdapter {

    @Override
    protected void onInitialize(DynamisAiEngine engine) {
        engine.governor().register(new AITaskNode(
            "example-adapter-heartbeat",
            1,
            Priority.LOW,
            DegradeMode.DEFER,
            () -> {
                // no-op task
            },
            () -> {
                // no-op fallback
            }
        ));
    }

    @Override
    protected void onShutdown() {
        // no-op
    }
}
