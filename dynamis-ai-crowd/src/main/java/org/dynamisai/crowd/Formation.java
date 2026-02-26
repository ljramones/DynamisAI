package org.dynamisai.crowd;

import org.dynamisai.navigation.NavPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes per-agent FormationSlots for a given FormationType.
 */
public final class Formation {

    /** Lateral spacing between agents in LINE and COLUMN formations (world units). */
    private static final float AGENT_SPACING = 1.5f;

    private Formation() {}

    /**
     * Compute formation slots for agentCount agents.
     */
    public static List<FormationSlot> compute(FormationType type,
                                               NavPoint centroid,
                                               NavPoint facing,
                                               int agentCount) {
        if (agentCount <= 0) return List.of();
        return switch (type) {
            case LINE -> line(centroid, facing, agentCount);
            case COLUMN -> column(centroid, facing, agentCount);
            case WEDGE -> wedge(centroid, facing, agentCount);
            case CIRCLE -> circle(centroid, facing, agentCount);
            case SCATTER -> scatter(centroid, facing, agentCount);
        };
    }

    private static List<FormationSlot> line(NavPoint centroid, NavPoint facing,
                                             int count) {
        float rx = facing.z();
        float rz = -facing.x();
        List<FormationSlot> slots = new ArrayList<>(count);
        float start = -((count - 1) * AGENT_SPACING) / 2f;
        for (int i = 0; i < count; i++) {
            float offset = start + i * AGENT_SPACING;
            NavPoint pos = NavPoint.of(
                centroid.x() + rx * offset,
                centroid.y(),
                centroid.z() + rz * offset);
            slots.add(new FormationSlot(i, pos, facing));
        }
        return slots;
    }

    private static List<FormationSlot> column(NavPoint centroid, NavPoint facing,
                                               int count) {
        List<FormationSlot> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float offset = -i * AGENT_SPACING;
            NavPoint pos = NavPoint.of(
                centroid.x() + facing.x() * offset,
                centroid.y(),
                centroid.z() + facing.z() * offset);
            slots.add(new FormationSlot(i, pos, facing));
        }
        return slots;
    }

    private static List<FormationSlot> wedge(NavPoint centroid, NavPoint facing,
                                              int count) {
        float rx = facing.z();
        float rz = -facing.x();
        List<FormationSlot> slots = new ArrayList<>(count);
        slots.add(new FormationSlot(0, centroid, facing));
        for (int i = 1; i < count; i++) {
            int rank = (i + 1) / 2;
            float side = (i % 2 == 1) ? 1f : -1f;
            float backOffset = rank * AGENT_SPACING;
            float sideOffset = rank * AGENT_SPACING * side;
            NavPoint pos = NavPoint.of(
                centroid.x() - facing.x() * backOffset + rx * sideOffset,
                centroid.y(),
                centroid.z() - facing.z() * backOffset + rz * sideOffset);
            slots.add(new FormationSlot(i, pos, facing));
        }
        return slots;
    }

    private static List<FormationSlot> circle(NavPoint centroid, NavPoint facing,
                                               int count) {
        float radius = (count <= 1) ? 0f
            : (AGENT_SPACING * count) / (2f * (float) Math.PI);
        List<FormationSlot> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count;
            NavPoint pos = NavPoint.of(
                centroid.x() + radius * (float) Math.cos(angle),
                centroid.y(),
                centroid.z() + radius * (float) Math.sin(angle));
            NavPoint face = pos.directionTo(centroid);
            slots.add(new FormationSlot(i, pos, face));
        }
        return slots;
    }

    private static List<FormationSlot> scatter(NavPoint centroid, NavPoint facing,
                                                int count) {
        List<FormationSlot> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float angle = (i * 137.508f) % 360f;
            float dist = AGENT_SPACING * (1 + i / 4f);
            float rad = (float) Math.toRadians(angle);
            NavPoint pos = NavPoint.of(
                centroid.x() + dist * (float) Math.cos(rad),
                centroid.y(),
                centroid.z() + dist * (float) Math.sin(rad));
            slots.add(new FormationSlot(i, pos, facing));
        }
        return slots;
    }
}
