package org.dynamisai.planning;

public final class CommonBehaviorTrees {

    private CommonBehaviorTrees() {}

    public static BehaviorTree guardPatrol() {
        BtNode root = new BtSelector(
            new BtSequence(
                new BtCondition("has-threat", c -> c.worldState().has(SquadFacts.THREAT_ENTITY)),
                new BtAction("alert-squad", c -> {
                    if (c.squadBlackboard() != null) {
                        c.squadBlackboard().writeRadio(
                            SquadFacts.BACKUP_REQUESTED, true, c.agent(), c.tick());
                    }
                    return BtStatus.SUCCESS;
                })
            ),
            new BtAction("patrol", c -> BtStatus.SUCCESS)
        );
        return new BehaviorTree("guard-patrol", root);
    }

    public static BehaviorTree combatEngagement() {
        BtNode root = new BtSelector(
            new BtSequence(
                new BtCondition("threat-present", c -> c.worldState().has(SquadFacts.THREAT_ENTITY)),
                new BtAction("take-cover", c -> c.worldState().has("cover.available") ? BtStatus.SUCCESS : BtStatus.FAILURE),
                new BtAction("attack", c -> BtStatus.SUCCESS)
            ),
            new BtAction("request-backup", c -> {
                if (c.squadBlackboard() != null) {
                    c.squadBlackboard().writeRadio(
                        SquadFacts.BACKUP_REQUESTED, true, c.agent(), c.tick());
                }
                return BtStatus.SUCCESS;
            })
        );
        return new BehaviorTree("combat-engagement", root);
    }

    public static BehaviorTree flee() {
        BtNode root = new BtSequence(
            new BtCondition("threat-present", c -> c.worldState().has(SquadFacts.THREAT_ENTITY)),
            new BtAction("move-away", c -> BtStatus.SUCCESS)
        );
        return new BehaviorTree("flee", root);
    }
}
