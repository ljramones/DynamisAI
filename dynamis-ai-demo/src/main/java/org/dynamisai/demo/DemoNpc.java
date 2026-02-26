package org.dynamisai.demo;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;

/**
 * Mutable NPC state for the demo.
 */
public final class DemoNpc {

    public final EntityId id;
    public final String name;
    public Location position;
    public AffectVector affect;
    public ThreatLevel perceivedThreat;
    public String lastDialogue = "";
    public String currentTask = "idle";
    public boolean isAlert = false;

    public DemoNpc(EntityId id, String name, Location startPos) {
        this.id = id;
        this.name = name;
        this.position = startPos;
        this.affect = AffectVector.neutral();
        this.perceivedThreat = ThreatLevel.NONE;
    }

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
