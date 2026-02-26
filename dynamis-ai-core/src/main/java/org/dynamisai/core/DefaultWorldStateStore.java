package org.dynamisai.core;

import io.vavr.collection.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public final class DefaultWorldStateStore implements WorldStateStore {

    private static final Logger log = LoggerFactory.getLogger(DefaultWorldStateStore.class);

    private static final int DEFAULT_BUFFER_SIZE = 300;

    private final int bufferSize;
    private final WorldSnapshot[] buffer;
    private volatile WorldSnapshot current;
    private final ConcurrentLinkedQueue<WorldChange> changeQueue = new ConcurrentLinkedQueue<>();

    public DefaultWorldStateStore(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new WorldSnapshot[bufferSize];
        this.current = new WorldSnapshot(
            0L,
            HashMap.empty(),
            new GlobalFacts(new java.util.HashMap<>()),
            new EnvironmentState("clear", 12.0f, 1.0f),
            Long.hashCode(0L)
        );
        storeInBuffer(this.current);
    }

    public DefaultWorldStateStore() {
        this(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public WorldSnapshot getCurrentSnapshot() {
        return current;
    }

    @Override
    public long getCurrentTick() {
        return current.tick();
    }

    /**
     * Drain the change queue and commit a new snapshot atomically.
     * Must be called from the simulation thread at the end of each tick.
     */
    @Override
    public void commitTick() {
        WorldSnapshot prev = current;

        HashMap<EntityId, EntityState> entities = prev.entities();
        GlobalFacts globalFacts = prev.globalFacts();
        EnvironmentState environment = prev.environment();

        WorldChange change;
        while ((change = changeQueue.poll()) != null) {
            switch (change) {
                case WorldChange.EntityStateChange c ->
                    entities = entities.put(c.id(), c.newState());
                case WorldChange.FactChange c -> {
                    java.util.Map<String, Object> updated = new java.util.HashMap<>(globalFacts.facts());
                    updated.put(c.key(), c.value());
                    globalFacts = new GlobalFacts(Collections.unmodifiableMap(updated));
                }
                case WorldChange.EnvironmentChange c ->
                    environment = c.newState();
                case WorldChange.RelationshipChange c -> {
                    EntityState existing = entities.get(c.a()).getOrNull();
                    if (existing != null) {
                        java.util.Map<String, Object> props = new java.util.HashMap<>(existing.properties());
                        props.put("rel:" + c.b().value() + ":" + c.relationshipKey(), c.value());
                        entities = entities.put(c.a(), new EntityState(c.a(), existing.position(), Collections.unmodifiableMap(props)));
                    }
                }
                case WorldChange.NarrativeRailsChange c -> {
                    java.util.Map<String, Object> updated = new java.util.HashMap<>(globalFacts.facts());
                    updated.put("narrativeRails", c.newRails());
                    globalFacts = new GlobalFacts(Collections.unmodifiableMap(updated));
                }
            }
        }

        long newTick = prev.tick() + 1;
        WorldSnapshot next = new WorldSnapshot(
            newTick, entities, globalFacts, environment, Long.hashCode(newTick)
        );
        current = next;
        storeInBuffer(next);
        log.trace("Committed world snapshot tick={}", newTick);
    }

    /**
     * Enqueue a change for the next commitTick(). Thread-safe — never blocks.
     */
    @Override
    public void enqueueChange(WorldChange change) {
        changeQueue.offer(change);
    }

    /**
     * Scoped query — never returns the full entity map.
     */
    @Override
    public WorldFacts query(EntityId agent, QueryScope scope) {
        WorldSnapshot snap = current;
        Location origin = scope.origin();
        float radius = scope.radius();

        List<EntityId> nearby = snap.entities().toJavaStream()
            .filter(e -> !e._1().equals(agent))
            .filter(e -> e._2().position().distanceTo(origin) <= radius)
            .map(e -> e._1())
            .collect(Collectors.toList());

        java.util.Map<String, Object> facts = new java.util.HashMap<>();
        ThreatLevel threat = ThreatLevel.NONE;
        NarrativeRails rails = null;

        if (scope.categories().contains(FactCategory.ALL) ||
            scope.categories().contains(FactCategory.THREAT)) {
            Object t = snap.globalFacts().facts().get("threatLevel");
            if (t instanceof ThreatLevel tl) {
                threat = tl;
            }
        }

        if (scope.includeRails()) {
            Object r = snap.globalFacts().facts().get("narrativeRails");
            if (r instanceof NarrativeRails nr) {
                rails = nr;
            }
        }

        snap.globalFacts().facts().forEach((k, v) -> {
            if (scope.categories().contains(FactCategory.ALL)) {
                facts.put(k, v);
            } else if (k.startsWith("spatial:") && scope.categories().contains(FactCategory.SPATIAL)) {
                facts.put(k, v);
            } else if (k.startsWith("env:") && scope.categories().contains(FactCategory.ENVIRONMENTAL)) {
                facts.put(k, v);
            }
        });

        EntityState agentState = snap.entities().get(agent).getOrNull();
        Location agentPos = agentState != null ? agentState.position() : origin;

        return new WorldFacts(Collections.unmodifiableMap(facts), nearby, threat, agentPos, rails);
    }

    /**
     * Time-travel retrieval. Returns null if the snapshot has been evicted.
     */
    @Override
    public WorldSnapshot getSnapshot(long tick) {
        int index = (int) (tick % bufferSize);
        WorldSnapshot candidate = buffer[index];
        if (candidate != null && candidate.tick() == tick) {
            return candidate;
        }
        return null;
    }

    private void storeInBuffer(WorldSnapshot snapshot) {
        int index = (int) (snapshot.tick() % bufferSize);
        buffer[index] = snapshot;
    }
}
