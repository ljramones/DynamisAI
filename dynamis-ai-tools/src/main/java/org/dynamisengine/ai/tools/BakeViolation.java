package org.dynamisengine.ai.tools;

public record BakeViolation(
    String assertionName,
    long tick,
    String message,
    String frameDetails
) {}
