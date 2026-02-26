package org.dynamisai.core;

public record EntityId(long value) {
    public static EntityId of(long value) { return new EntityId(value); }
}
