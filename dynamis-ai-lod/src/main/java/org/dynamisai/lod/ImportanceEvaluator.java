package org.dynamisai.lod;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.WorldSnapshot;

public interface ImportanceEvaluator {

    ImportanceScore evaluate(EntityId entityId, WorldSnapshot snapshot);
}
