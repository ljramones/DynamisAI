package org.dynamisengine.ai.lod;

import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.WorldSnapshot;

public interface ImportanceEvaluator {

    ImportanceScore evaluate(EntityId entityId, WorldSnapshot snapshot);
}
