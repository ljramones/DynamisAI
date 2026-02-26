package org.dynamisai.cognition;

import org.dynamisai.core.EntityId;

public record SecondOrderBelief(
    EntityId holder,
    EntityId subject,
    String key,
    float estimatedConfidence,
    long formedAtTick
) {}
