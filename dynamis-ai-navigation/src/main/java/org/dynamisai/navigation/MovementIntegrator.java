package org.dynamisai.navigation;

import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.WorldChange;
import org.dynamisai.core.WorldStateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Commits CrowdSnapshot positions into WorldStateStore each tick.
 *
 * Uses reflection against CrowdSnapshot accessors intentionally.
 * A direct type import would require a navigation -> crowd JPMS edge,
 * but crowd already depends on navigation, creating a module cycle.
 */
public final class MovementIntegrator {

    private static final Logger log =
        LoggerFactory.getLogger(MovementIntegrator.class);

    private final WorldStateStore store;

    public MovementIntegrator(WorldStateStore store) {
        this.store = store;
    }

    public void integrate(Object snapshot, long tick, EntityState... extras) {
        int committed = 0;

        if (snapshot != null) {
            try {
                Method groupsMethod = snapshot.getClass().getMethod("groups");
                @SuppressWarnings("unchecked")
                Map<Object, Object> groups = (Map<Object, Object>) groupsMethod.invoke(snapshot);
                for (Object group : groups.values()) {
                    String lodName = String.valueOf(group.getClass().getMethod("lod").invoke(group));
                    @SuppressWarnings("unchecked")
                    List<Object> agents = (List<Object>) group.getClass().getMethod("agents").invoke(group);
                    for (Object agent : agents) {
                        EntityId id = (EntityId) agent.getClass().getMethod("id").invoke(agent);
                        Object pos = agent.getClass().getMethod("position").invoke(agent);
                        Object vel = agent.getClass().getMethod("velocity").invoke(agent);
                        Location loc = (Location) pos.getClass().getMethod("toLocation").invoke(pos);
                        float vx = ((Number) vel.getClass().getMethod("x").invoke(vel)).floatValue();
                        float vz = ((Number) vel.getClass().getMethod("z").invoke(vel)).floatValue();
                        int slot = ((Number) agent.getClass().getMethod("slotIndex").invoke(agent)).intValue();

                        store.enqueueChange(new WorldChange.EntityStateChange(
                            id,
                            new EntityState(id, loc, Map.of(
                                "velocity_x", vx,
                                "velocity_z", vz,
                                "lod", lodName,
                                "slot", slot
                            ))
                        ));
                        committed++;
                    }
                }
            } catch (ReflectiveOperationException e) {
                log.warn("MovementIntegrator: snapshot reflection failed: {}", e.getMessage());
            }
        }

        for (EntityState extra : extras) {
            store.enqueueChange(new WorldChange.EntityStateChange(extra.id(), extra));
            committed++;
        }

        store.commitTick();
        log.debug("MovementIntegrator: tick={} committed={}", tick, committed);
    }

    public static EntityState externalEntity(EntityId id, Location position,
                                             Map<String, Object> metadata) {
        return new EntityState(id, position, metadata);
    }
}
