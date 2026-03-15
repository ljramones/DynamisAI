package org.dynamisengine.ai.perception;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.QueryScope;
import org.dynamisengine.ai.core.WorldFacts;
import org.dynamisengine.ai.core.WorldStateStore;
import org.dynamisengine.core.logging.DynamisLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultPerceptionSystem implements PerceptionSystem {

    private static final DynamisLogger log = DynamisLogger.get(DefaultPerceptionSystem.class);

    private final SaliencyFilter filter;
    private final Map<EntityId, PerceptionSnapshot> lastSnapshots = new ConcurrentHashMap<>();

    public DefaultPerceptionSystem(SaliencyFilter filter) {
        this.filter = filter;
    }

    public DefaultPerceptionSystem() {
        this(new DefaultSaliencyFilter());
    }

    @Override
    public PerceptionSnapshot tick(EntityId owner, AffectVector currentMood,
                                   WorldStateStore store) {
        Location origin = lastSnapshots.getOrDefault(owner,
            PerceptionSnapshot.empty(owner, -1L, new Location(0, 0, 0))).ownerLocation();

        QueryScope scope = QueryScope.tactical(origin);
        WorldFacts facts = store.query(owner, scope);

        PerceptionSnapshot snapshot = filter.filter(owner, facts, currentMood,
            store.getCurrentTick());
        lastSnapshots.put(owner, snapshot);

        log.debug(String.format("Perception tick for %s — %s percepts, threat=%s", owner, snapshot.percepts().size(), snapshot.aggregateThreat()));

        return snapshot;
    }

    @Override
    public PerceptionSnapshot getLastSnapshot(EntityId owner) {
        return lastSnapshots.getOrDefault(owner,
            PerceptionSnapshot.empty(owner, -1L, new Location(0, 0, 0)));
    }
}
