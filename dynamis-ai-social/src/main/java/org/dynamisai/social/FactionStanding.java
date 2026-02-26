package org.dynamisai.social;

/**
 * An entity's standing with one faction.
 *
 * standing: [-1, 1]
 *   -1.0 = KOS (kill on sight)
 *    0.0 = unknown / neutral
 *   +1.0 = exalted / champion
 *
 * Rank is a human-readable label assigned by the faction's rank ladder.
 */
public record FactionStanding(
    FactionId faction,
    float standing,
    String rank,
    long contributionPoints
) {
    public FactionStanding {
        standing = Math.max(-1f, Math.min(1f, standing));
    }

    public static FactionStanding unknown(FactionId faction) {
        return new FactionStanding(faction, 0f, "Stranger", 0L);
    }

    public FactionStanding withStanding(float newStanding) {
        return new FactionStanding(faction, newStanding, rank, contributionPoints);
    }

    public FactionStanding withRank(String newRank) {
        return new FactionStanding(faction, standing, newRank, contributionPoints);
    }

    public FactionStanding addPoints(long points) {
        return new FactionStanding(faction, standing, rank,
            contributionPoints + points);
    }

    public boolean isHostile()  { return standing < -0.25f; }
    public boolean isFriendly() { return standing >  0.25f; }
    public boolean isNeutral()  { return !isHostile() && !isFriendly(); }
}
