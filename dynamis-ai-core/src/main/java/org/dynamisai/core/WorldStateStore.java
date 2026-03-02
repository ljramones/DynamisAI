package org.dynamisai.core;

import org.dynamis.core.entity.EntityId;

public interface WorldStateStore {
    WorldSnapshot getCurrentSnapshot();
    WorldFacts query(EntityId agent, QueryScope scope);
    void enqueueChange(WorldChange change);
    void commitTick();
    WorldSnapshot getSnapshot(long tick);
    long getCurrentTick();
}
