package org.dynamisai.tools;

import org.dynamisai.core.AIOutputFrame;

import java.util.Optional;

public interface BakeAssertion {
    String name();
    Optional<String> evaluate(long tick, AIOutputFrame frame);
}
