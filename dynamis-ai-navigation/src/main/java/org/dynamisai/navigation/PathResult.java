package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;

/**
 * Sealed result of a pathfinding request.
 * Pattern-matched by HTN planner movement primitives.
 */
public sealed interface PathResult
    permits PathResult.Found,
            PathResult.Unreachable,
            PathResult.Partial {

    EntityId requester();

    /** Full path found to goal. */
    record Found(EntityId requester, NavPath path) implements PathResult {}

    /** Goal is unreachable — no connected path exists. */
    record Unreachable(EntityId requester, String reason) implements PathResult {}

    /** Partial path found — budget exhausted before reaching goal. */
    record Partial(EntityId requester, NavPath path, String reason) implements PathResult {}
}
