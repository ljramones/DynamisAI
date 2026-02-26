package org.dynamisai.core;

import java.util.Set;

public record QueryScope(
    Location origin,
    float radius,
    Set<FactCategory> categories,
    boolean includeRails,
    boolean includeRelations
) {
    public static QueryScope perception(Location origin) {
        return new QueryScope(origin, 50f,
            Set.of(FactCategory.SPATIAL, FactCategory.THREAT),
            false, false);
    }

    public static QueryScope dialogue(Location origin) {
        return new QueryScope(origin, 10f,
            Set.of(FactCategory.ALL),
            true, true);
    }

    public static QueryScope tactical(Location origin) {
        return new QueryScope(origin, 80f,
            Set.of(FactCategory.SPATIAL, FactCategory.THREAT, FactCategory.RELATIONSHIP),
            false, true);
    }
}
