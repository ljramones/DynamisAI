package org.dynamisai.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for engine adapters.
 */
public abstract class AbstractGameEngineAdapter implements GameEngineAdapter {

    private static final Logger log =
        LoggerFactory.getLogger(AbstractGameEngineAdapter.class);

    protected DynamisAiEngine engine;

    @Override
    public final void initialize(DynamisAiEngine engine) {
        this.engine = engine;
        engine.initialize();
        onInitialize(engine);
        log.info("{} initialized", getClass().getSimpleName());
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
        log.info("{} shutdown", getClass().getSimpleName());
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
