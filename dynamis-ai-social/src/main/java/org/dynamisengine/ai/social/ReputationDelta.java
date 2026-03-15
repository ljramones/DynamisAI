package org.dynamisengine.ai.social;

import java.util.Set;

public record ReputationDelta(
    float trustDelta,
    float affinityDelta,
    Set<RelationshipTag> tagsToAdd,
    Set<RelationshipTag> tagsToRemove
) {
    public ReputationDelta {
        tagsToAdd = Set.copyOf(tagsToAdd);
        tagsToRemove = Set.copyOf(tagsToRemove);
    }

    public static ReputationDelta zero() {
        return new ReputationDelta(0f, 0f, Set.of(), Set.of());
    }
}
