package org.dynamisai.social;

/**
 * Derived influence modifiers computed from SocialContext.
 */
public record SocialInfluence(
    float temperatureModifier,
    float affinityBias,
    float trustBias,
    boolean allowPersonalTopics,
    boolean suppressHostileOptions,
    boolean forceCompliance
) {
    /** Neutral influence — no modifiers applied. */
    public static SocialInfluence neutral() {
        return new SocialInfluence(1.0f, 0f, 0f, false, false, false);
    }

    /**
     * Derive influence from a SocialContext.
     */
    public static SocialInfluence from(SocialContext ctx) {
        float temp = 1.0f;
        if (ctx.relationship().hasTag(RelationshipTag.ENEMY) ||
            ctx.relationship().hasTag(RelationshipTag.BETRAYED)) {
            temp = 0.7f;
        }
        if (ctx.relationship().hasTag(RelationshipTag.TRUSTED) ||
            ctx.relationship().hasTag(RelationshipTag.ALLY)) {
            temp = 1.2f;
        }

        return new SocialInfluence(
            temp,
            ctx.affinityBias(),
            ctx.relationship().trust() * 0.3f,
            ctx.shouldShareInformation(),
            ctx.suppressThreatEscalation(),
            ctx.shouldComplyReadily()
        );
    }
}
