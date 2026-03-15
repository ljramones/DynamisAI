package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.core.AIOutputFrame;

import java.util.Optional;

public interface BakeAssertion {
    String name();
    Optional<String> evaluate(long tick, AIOutputFrame frame);
}
