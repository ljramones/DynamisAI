package org.dynamisai.core;

import org.dynamis.core.entity.EntityId;
import java.util.Map;

public record EntityState(
    EntityId id,
    Location position,
    Map<String, Object> properties
) {}
