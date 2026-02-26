package org.dynamisai.perception;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.QueryScope;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.core.WorldStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultPerceptionSystem implements PerceptionSystem {

    private static final Logger log = LoggerFactory.getLogger(DefaultPerceptionSystem.class);

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

        log.debug("Perception tick for {} — {} percepts, threat={}",
            owner, snapshot.percepts().size(), snapshot.aggregateThreat());

        return snapshot;
    }

    @Override
    public PerceptionSnapshot getLastSnapshot(EntityId owner) {
        return lastSnapshots.getOrDefault(owner,
            PerceptionSnapshot.empty(owner, -1L, new Location(0, 0, 0)));
    }
}
