package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;

public record SecondOrderBelief(
    EntityId holder,
    EntityId subject,
    String key,
    float estimatedConfidence,
    long formedAtTick
) {}
