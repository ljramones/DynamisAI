package org.dynamisai.social;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueRequest;
import org.dynamisai.core.EntityId;

/**
 * Shapes a DialogueRequest using SocialInfluence derived from SocialContext.
 */
public final class SocialDialogueShaper {

    private SocialDialogueShaper() {}

    /**
     * Shape a DialogueRequest using current social state between npc and target.
     */
    public static DialogueRequest shape(DialogueRequest request,
                                        SocialSystem social,
                                        EntityId npc,
                                        EntityId target) {
        SocialContext ctx = social.buildContext(npc, target);
        SocialInfluence influence = SocialInfluence.from(ctx);

        AffectVector baseMood = request.currentMood();
        AffectVector shapedMood = new AffectVector(
            clampSigned(baseMood.valence() + influence.affinityBias()),
            baseMood.arousal(),
            clampUnit(baseMood.dominance() + influence.trustBias()),
            baseMood.sarcasm(),
            baseMood.intensity()
        );

        String hint = buildHint(ctx, influence);
        String shapedInput = hint.isEmpty()
            ? request.inputSpeech()
            : "[" + hint + "] " + request.inputSpeech();

        return new DialogueRequest(
            request.speaker(),
            request.target(),
            shapedInput,
            request.context(),
            shapedMood,
            request.snapshot()
        );
    }

    /**
     * Convenience: shape using default npc=speaker, target=request.target().
     */
    public static DialogueRequest shape(DialogueRequest request,
                                        SocialSystem social) {
        return shape(request, social, request.speaker(), request.target());
    }

    private static String buildHint(SocialContext ctx, SocialInfluence influence) {
        if (!ctx.isKnownEntity()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Relationship rel = ctx.relationship();

        if (rel.hasTag(RelationshipTag.ALLY)) sb.append("ally ");
        if (rel.hasTag(RelationshipTag.ENEMY)) sb.append("enemy ");
        if (rel.hasTag(RelationshipTag.TRUSTED)) sb.append("trusted ");
        if (rel.hasTag(RelationshipTag.FEARED)) sb.append("feared ");
        if (rel.hasTag(RelationshipTag.INDEBTED)) sb.append("indebted ");
        if (rel.hasTag(RelationshipTag.BETRAYED)) sb.append("betrayed ");

        if (influence.suppressHostileOptions()) sb.append("no-threat ");
        if (influence.allowPersonalTopics()) sb.append("personal-ok ");
        if (influence.forceCompliance()) sb.append("comply ");

        ctx.lastDialogue().ifPresent(d ->
            sb.append("last:").append(d.topic()).append(" "));

        return sb.toString().trim();
    }

    private static float clampSigned(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }

    private static float clampUnit(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
