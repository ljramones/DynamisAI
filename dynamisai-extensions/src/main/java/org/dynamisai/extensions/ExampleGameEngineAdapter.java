package org.dynamisai.extensions;

import org.dynamisai.core.AITaskNode;
import org.dynamisai.core.AbstractGameEngineAdapter;
import org.dynamisai.core.DegradeMode;
import org.dynamisai.core.DynamisAiEngine;
import org.dynamisai.core.Priority;

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
