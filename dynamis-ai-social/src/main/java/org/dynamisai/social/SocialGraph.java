package org.dynamisai.social;

import org.dynamisai.core.EntityId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Directed relationship graph between entities.
 *
 * Edges are directed — A→B and B→A stored separately.
 * Missing edges are treated as Relationship.neutral().
 *
 * Thread-safe: ConcurrentHashMap with compute() for atomic updates.
 */
public final class SocialGraph {

    // Key: "fromId:toId"
    private final ConcurrentHashMap<String, Relationship> edges = new ConcurrentHashMap<>();

    /**
     * Get the relationship from → to.
     * Returns Relationship.neutral() if no edge exists.
     */
    public Relationship get(EntityId from, EntityId to) {
        return edges.getOrDefault(edgeKey(from, to),
            Relationship.neutral(from, to));
    }

    /**
     * Set the relationship from → to.
     * Does not affect the reverse edge (to → from).
     */
    public void set(EntityId from, EntityId to, Relationship relationship) {
        edges.put(edgeKey(from, to), relationship);
    }

    /**
     * Update the relationship from → to via a function.
     * Creates a neutral baseline if no edge exists.
     */
    public void update(EntityId from, EntityId to,
                        java.util.function.UnaryOperator<Relationship> updater) {
        edges.compute(edgeKey(from, to), (k, existing) -> {
            Relationship base = existing != null
                ? existing : Relationship.neutral(from, to);
            return updater.apply(base);
        });
    }

    /**
     * Adjust trust for both directed edges symmetrically.
     * Clamps to [-1, 1].
     */
    public void adjustTrust(EntityId a, EntityId b, float delta) {
        update(a, b, r -> r.withTrust(r.trust() + delta)
                           .recordInteraction(Instant.now()));
        update(b, a, r -> r.withTrust(r.trust() + delta)
                           .recordInteraction(Instant.now()));
    }

    /**
     * Adjust affinity for both directed edges symmetrically.
     */
    public void adjustAffinity(EntityId a, EntityId b, float delta) {
        update(a, b, r -> r.withAffinity(r.affinity() + delta)
                           .recordInteraction(Instant.now()));
        update(b, a, r -> r.withAffinity(r.affinity() + delta)
                           .recordInteraction(Instant.now()));
    }

    /**
     * Add a tag to the from→to directed edge only.
     * Use addTagBoth() for reciprocal tagging.
     */
    public void addTag(EntityId from, EntityId to, RelationshipTag tag) {
        update(from, to, r -> r.withTag(tag));
    }

    /** Add a tag to both directed edges. */
    public void addTagBoth(EntityId a, EntityId b, RelationshipTag tag) {
        addTag(a, b, tag);
        addTag(b, a, tag);
    }

    /** Remove a tag from both directed edges. */
    public void removeTagBoth(EntityId a, EntityId b, RelationshipTag tag) {
        update(a, b, r -> r.withoutTag(tag));
        update(b, a, r -> r.withoutTag(tag));
    }

    /** Remove all edges for an entity — call on despawn. */
    public void removeEntity(EntityId entity) {
        edges.keySet().removeIf(k ->
            k.startsWith(entity.value() + ":") ||
            k.endsWith(":" + entity.value()));
    }

    /** All entities that have at least one outgoing edge from the given entity. */
    public List<EntityId> knownEntities(EntityId from) {
        String prefix = from.value() + ":";
        List<EntityId> result = new ArrayList<>();
        for (String key : edges.keySet()) {
            if (key.startsWith(prefix)) {
                long toId = Long.parseLong(key.substring(prefix.length()));
                result.add(EntityId.of(toId));
            }
        }
        return result;
    }

    /** Total directed edge count in the graph. */
    public int edgeCount() { return edges.size(); }

    private static String edgeKey(EntityId from, EntityId to) {
        return from.value() + ":" + to.value();
    }
}
