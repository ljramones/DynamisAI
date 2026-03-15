package org.dynamisengine.ai.core;

/**
 * Minimal adapter used by demos/tests without rendering or hardware audio.
 */
public final class HeadlessGameEngineAdapter extends AbstractGameEngineAdapter {

    @Override
    protected void onInitialize(DynamisAiEngine engine) {
        // no-op
    }

    @Override
    protected void onShutdown() {
        // no-op
    }
}
