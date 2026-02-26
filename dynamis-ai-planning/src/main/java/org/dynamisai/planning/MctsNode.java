package org.dynamisai.planning;

import java.util.ArrayList;
import java.util.List;

final class MctsNode {
    final WorldState state;
    final GoapAction actionFromParent;
    final MctsNode parent;
    final List<MctsNode> children = new ArrayList<>();
    int visits;
    double totalReward;

    MctsNode(WorldState state, GoapAction actionFromParent, MctsNode parent) {
        this.state = state;
        this.actionFromParent = actionFromParent;
        this.parent = parent;
    }

    double ucb1(int totalSimulations, double explorationConstant) {
        if (visits == 0) {
            return Double.MAX_VALUE;
        }
        return (totalReward / visits)
            + explorationConstant * Math.sqrt(Math.log(Math.max(1, totalSimulations)) / visits);
    }
}
