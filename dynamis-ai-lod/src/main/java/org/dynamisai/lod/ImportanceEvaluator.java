package org.dynamisai.lod;

import org.dynamis.core.entity.EntityId;
import org.dynamisai.core.WorldSnapshot;

public interface ImportanceEvaluator {

    ImportanceScore evaluate(EntityId entityId, WorldSnapshot snapshot);
}
