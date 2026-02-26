package org.dynamisai.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified ORCA (RVO2) velocity solver.
 *
 * For each pair of agents, computes an ORCA half-plane constraint in velocity space.
 * Finds the velocity closest to preferredVelocity that satisfies all constraints.
 *
 * All computations in XZ plane — Y component of velocities is ignored and preserved.
 *
 * Reference: van den Berg et al., "Reciprocal n-Body Collision Avoidance" (2011).
 */
public final class RvoSolver {

    /** Time horizon for velocity obstacle computation (seconds). */
    private static final float TAU = 2.5f;

    /** Epsilon for geometric tests. */
    private static final float EPS = 1e-5f;

    /**
     * Compute collision-free velocities for all agents.
     * Returns updated RvoAgents with new velocity fields.
     * Immutable input — produces new RvoAgent instances.
     *
     * @param agents All agents in the local neighbourhood
     * @param deltaT Simulation timestep in seconds
     */
    public List<RvoAgent> solve(List<RvoAgent> agents, float deltaT) {
        List<RvoAgent> result = new ArrayList<>(agents.size());
        for (RvoAgent agent : agents) {
            NavPoint newVel = computeNewVelocity(agent, agents, deltaT);
            result.add(agent.withVelocity(newVel));
        }
        return result;
    }

    private NavPoint computeNewVelocity(RvoAgent agent, List<RvoAgent> others,
                                        float deltaT) {
        List<float[]> orcaLines = new ArrayList<>();

        for (RvoAgent other : others) {
            if (other.id().equals(agent.id())) {
                continue;
            }

            float relPx = other.position().x() - agent.position().x();
            float relPz = other.position().z() - agent.position().z();
            float relVx = agent.velocity().x() - other.velocity().x();
            float relVz = agent.velocity().z() - other.velocity().z();

            float combinedRadius = agent.radius() + other.radius();
            float distSq = relPx * relPx + relPz * relPz;
            float dist = (float) Math.sqrt(distSq);

            float[] orca = computeOrcaLine(
                relPx, relPz, relVx, relVz, combinedRadius, dist, deltaT);
            if (orca != null) {
                orcaLines.add(orca);
            }
        }

        float prefX = agent.preferredVelocity().x();
        float prefZ = agent.preferredVelocity().z();
        float maxSpeed = agent.maxSpeed();

        float[] vel = linearProgram(orcaLines, prefX, prefZ, maxSpeed);

        return NavPoint.of(vel[0], agent.preferredVelocity().y(), vel[1]);
    }

    /**
     * Compute ORCA half-plane for one agent-neighbour pair.
     * Returns [point_x, point_z, dir_x, dir_z] of the ORCA line, or null if no constraint.
     */
    private float[] computeOrcaLine(float relPx, float relPz,
                                    float relVx, float relVz,
                                    float combinedRadius, float dist,
                                    float deltaT) {
        if (dist < EPS) {
            return null;
        }

        float invTau = 1.0f / TAU;

        float voApexX = relVx - invTau * relPx;
        float voApexZ = relVz - invTau * relPz;

        float apexDistSq = voApexX * voApexX + voApexZ * voApexZ;
        float rTau = combinedRadius * invTau;

        float u;
        float nx;
        float nz;

        if (apexDistSq < rTau * rTau) {
            float apexDist = (float) Math.sqrt(apexDistSq);
            if (apexDist < EPS) {
                nx = -relPz / dist;
                nz = relPx / dist;
            } else {
                nx = voApexX / apexDist;
                nz = voApexZ / apexDist;
            }
            u = rTau - apexDist;
        } else {
            float dot = relPx * voApexX + relPz * voApexZ;
            float proj = dot / (dist * dist);
            float closestX = proj * relPx - voApexX;
            float closestZ = proj * relPz - voApexZ;
            float closestDist = (float) Math.sqrt(closestX * closestX + closestZ * closestZ);
            if (closestDist < EPS) {
                return null;
            }
            nx = closestX / closestDist;
            nz = closestZ / closestDist;
            u = rTau - closestDist;
        }

        float px = 0.5f * u * nx;
        float pz = 0.5f * u * nz;
        float dx = -nz;
        float dz = nx;

        return new float[]{px, pz, dx, dz};
    }

    /**
     * Linear program: find velocity in disk(maxSpeed) closest to (prefX, prefZ)
     * that satisfies all half-plane constraints.
     * Simplified: iterative projection (not full LP — sufficient for ≤ 20 agents).
     */
    private float[] linearProgram(List<float[]> lines, float prefX, float prefZ,
                                  float maxSpeed) {
        float vx = prefX;
        float vz = prefZ;

        float speed = (float) Math.sqrt(vx * vx + vz * vz);
        if (speed > maxSpeed) {
            vx = vx / speed * maxSpeed;
            vz = vz / speed * maxSpeed;
        }

        for (float[] line : lines) {
            float px = line[0];
            float pz = line[1];
            float dx = line[2];
            float dz = line[3];

            float nx = -dz;
            float nz = dx;
            float dot = (vx - px) * nx + (vz - pz) * nz;

            if (dot < 0) {
                float t = (vx - px) * dx + (vz - pz) * dz;
                vx = px + t * dx;
                vz = pz + t * dz;

                speed = (float) Math.sqrt(vx * vx + vz * vz);
                if (speed > maxSpeed) {
                    vx = vx / speed * maxSpeed;
                    vz = vz / speed * maxSpeed;
                }
            }
        }
        return new float[]{vx, vz};
    }
}
