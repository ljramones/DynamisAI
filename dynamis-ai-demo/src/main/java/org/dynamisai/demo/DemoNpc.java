package org.dynamisai.demo;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;

/**
 * Mutable NPC state for the demo.
 */
public final class DemoNpc {

    /** The unique identifier of this NPC. */
    public final EntityId id;
    /** The display name of this NPC. */
    public final String name;
    /** The current 3D location of this NPC in the world. */
    public Location position;
    /** The current emotional state of this NPC. */
    public AffectVector affect;
    /** The level of threat this NPC perceives in its environment. */
    public ThreatLevel perceivedThreat;
    /** The last line of dialogue spoken by this NPC. */
    public String lastDialogue = "";
    /** The current high-level task or behavior state of this NPC. */
    public String currentTask = "idle";
    /** Whether this NPC is currently in a state of high alertness. */
    public boolean isAlert = false;

    /**
     * Creates a new DemoNpc with the specified identity and starting position.
     *
     * @param id       The unique identifier for the NPC.
     * @param name     The display name for the NPC.
     * @param startPos The initial location in the world.
     */
    public DemoNpc(EntityId id, String name, Location startPos) {
        this.id = id;
        this.name = name;
        this.position = startPos;
        this.affect = AffectVector.neutral();
        this.perceivedThreat = ThreatLevel.NONE;
    }

    /**
     * Calculates the Euclidean distance from this NPC's current position to another location.
     *
     * @param other The target location.
     * @return The distance to the target location.
     */
    public float distanceTo(Location other) {
        float dx = (float) (position.x() - other.x());
        float dy = (float) (position.y() - other.y());
        float dz = (float) (position.z() - other.z());
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String toString() {
        return name + "[" + id.value() + "]";
    }
}
