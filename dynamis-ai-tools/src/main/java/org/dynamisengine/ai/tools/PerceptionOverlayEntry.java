package org.dynamisengine.ai.tools;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;

public record PerceptionOverlayEntry(
    EntityId perceivedEntity,
    String stimulusType,
    float intensity,
    boolean isSalient,
    Location position
) {}
