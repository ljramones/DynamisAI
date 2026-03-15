package org.dynamisengine.ai.planning;

import java.util.List;

public final class CommonGoapActions {

    private CommonGoapActions() {}

    public static GoapAction moveTo() {
        return GoapAction.of(
            "move-to",
            s -> s.has("target.position"),
            List.of(new PlannerEffect("at.target", true)),
            0.4f
        );
    }

    public static GoapAction attackThreat() {
        return GoapAction.of(
            "attack-threat",
            s -> s.has(SquadFacts.THREAT_ENTITY),
            List.of(new PlannerEffect("threat.engaged", true)),
            0.8f
        );
    }

    public static GoapAction takeCover() {
        return GoapAction.of(
            "take-cover",
            s -> s.has("cover.available"),
            List.of(new PlannerEffect("in.cover", true)),
            0.5f
        );
    }

    public static GoapAction requestBackup() {
        return GoapAction.of(
            "request-backup",
            s -> true,
            List.of(new PlannerEffect(SquadFacts.BACKUP_REQUESTED, true)),
            0.3f
        );
    }

    public static GoapAction flee() {
        return GoapAction.of(
            "flee",
            s -> s.has(SquadFacts.THREAT_POSITION),
            List.of(new PlannerEffect("is.fleeing", true)),
            0.7f
        );
    }

    public static GoapAction waitAction() {
        return GoapAction.of("wait", s -> true, List.of(), 0f);
    }
}
