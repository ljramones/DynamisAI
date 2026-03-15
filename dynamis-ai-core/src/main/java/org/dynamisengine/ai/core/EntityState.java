package org.dynamisengine.ai.core;

import org.dynamisengine.core.entity.EntityId;
import java.util.Map;

public record EntityState(
    EntityId id,
    Location position,
    Map<String, Object> properties
) {}
