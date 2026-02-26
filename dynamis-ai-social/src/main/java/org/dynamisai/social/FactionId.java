package org.dynamisai.social;

/**
 * Strongly-typed faction identifier.
 * Factions are named groups that NPCs have standing with —
 * city guards, thieves guild, merchant consortium, etc.
 */
public record FactionId(String name) {

    public FactionId {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("FactionId name must not be blank");
        }
    }

    public static FactionId of(String name) { return new FactionId(name); }

    @Override public String toString() { return "Faction[" + name + "]"; }
}
