package org.dynamisai.tools;

public record BakeViolation(
    String assertionName,
    long tick,
    String message,
    String frameDetails
) {}
