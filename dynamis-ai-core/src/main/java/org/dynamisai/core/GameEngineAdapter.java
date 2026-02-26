package org.dynamisai.core;

/**
 * SPI for integrating DynamisAiEngine into an external game loop.
 */
public interface GameEngineAdapter {
    void initialize(DynamisAiEngine engine);
    AIOutputFrame tick(GameEngineContext context);
    void shutdown();
}
