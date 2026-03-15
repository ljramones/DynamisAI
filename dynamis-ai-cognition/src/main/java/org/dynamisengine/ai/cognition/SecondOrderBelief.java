package org.dynamisengine.ai.cognition;

import org.dynamisengine.core.entity.EntityId;

public record SecondOrderBelief(
    EntityId holder,
    EntityId subject,
    String key,
    float estimatedConfidence,
    long formedAtTick
) {}
