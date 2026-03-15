package org.dynamisengine.ai.core;

import org.dynamisengine.core.logging.DynamisLogger;

/**
 * Base implementation for engine adapters.
 */
public abstract class AbstractGameEngineAdapter implements GameEngineAdapter {

    private static final DynamisLogger log = DynamisLogger.get(AbstractGameEngineAdapter.class);

    protected DynamisAiEngine engine;

    @Override
    public final void initialize(DynamisAiEngine engine) {
        this.engine = engine;
        engine.initialize();
        onInitialize(engine);
        log.info(String.format("%s initialized", getClass().getSimpleName()));
    }

    @Override
    public final AIOutputFrame tick(GameEngineContext context) {
        if (engine == null) {
            throw new IllegalStateException("GameEngineAdapter.tick() called before initialize()");
        }
        onPreTick(context);
        AIOutputFrame frame = engine.tick(context);
        onPostTick(context, frame);
        return frame;
    }

    @Override
    public final void shutdown() {
        onShutdown();
        if (engine != null) {
            engine.shutdown();
        }
        log.info(String.format("%s shutdown", getClass().getSimpleName()));
    }

    protected abstract void onInitialize(DynamisAiEngine engine);

    protected void onPreTick(GameEngineContext context) {
        // optional
    }

    protected void onPostTick(GameEngineContext context, AIOutputFrame frame) {
        // optional
    }

    protected abstract void onShutdown();
}
