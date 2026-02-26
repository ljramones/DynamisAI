package org.dynamisai.core;

import java.util.Map;

public record EntityState(
    EntityId id,
    Location position,
    Map<String, Object> properties
) {}
