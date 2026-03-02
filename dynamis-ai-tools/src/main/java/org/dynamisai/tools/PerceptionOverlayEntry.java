package org.dynamisai.tools;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.Location;

public record PerceptionOverlayEntry(
    EntityId perceivedEntity,
    String stimulusType,
    float intensity,
    boolean isSalient,
    Location position
) {}
