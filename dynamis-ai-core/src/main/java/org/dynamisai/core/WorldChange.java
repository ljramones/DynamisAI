package org.dynamisai.core;

public sealed interface WorldChange
    permits WorldChange.EntityStateChange,
            WorldChange.FactChange,
            WorldChange.EnvironmentChange,
            WorldChange.RelationshipChange,
            WorldChange.NarrativeRailsChange {

    record EntityStateChange(EntityId id, EntityState newState) implements WorldChange {}
    record FactChange(String key, Object value) implements WorldChange {}
    record EnvironmentChange(EnvironmentState newState) implements WorldChange {}
    record RelationshipChange(EntityId a, EntityId b, String relationshipKey, Object value) implements WorldChange {}
    record NarrativeRailsChange(NarrativeRails newRails) implements WorldChange {}
}
