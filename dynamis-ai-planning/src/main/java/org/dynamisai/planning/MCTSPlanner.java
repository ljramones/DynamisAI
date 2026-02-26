package org.dynamisai.planning;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class MCTSPlanner {

    public static final int DEFAULT_MAX_NODES = 64;
    public static final double DEFAULT_EXPLORATION = 1.414;
    private static final int ROLLOUT_DEPTH = 5;

    private final GoapActionLibrary actionLibrary;
    private final int maxNodes;
    private final double explorationConstant;
    private int lastExpansionCount;

    public MCTSPlanner(GoapActionLibrary actionLibrary) {
        this(actionLibrary, DEFAULT_MAX_NODES, DEFAULT_EXPLORATION);
    }

    public MCTSPlanner(GoapActionLibrary actionLibrary,
                       int maxNodes,
                       double explorationConstant) {
        this.actionLibrary = actionLibrary;
        this.maxNodes = maxNodes;
        this.explorationConstant = explorationConstant;
    }

    public Optional<GoapAction> selectAction(
        GoapGoal goal,
        WorldState currentState,
        long deterministicSeed,
        PlanningBudget budget
    ) {
        lastExpansionCount = 0;

        if (goal.isSatisfied().test(currentState)) {
            return Optional.empty();
        }

        List<GoapAction> applicableRoot = actionLibrary.applicableTo(currentState);
        if (applicableRoot.isEmpty()) {
            return Optional.empty();
        }

        int nodeLimit = Math.min(maxNodes, budget.maxNodes());
        MctsNode root = new MctsNode(currentState, null, null);

        Random random = new Random(deterministicSeed ^ currentState.tick());

        while (lastExpansionCount < nodeLimit) {
            MctsNode selected = select(root);
            MctsNode expanded = expand(selected, nodeLimit);
            if (expanded == null) {
                break;
            }
            double reward = simulate(expanded, goal, random, deterministicSeed);
            backpropagate(expanded, reward);
        }

        return root.children.stream()
            .max(Comparator.comparingInt(c -> c.visits))
            .map(c -> c.actionFromParent);
    }

    int lastExpansionCount() {
        return lastExpansionCount;
    }

    private MctsNode select(MctsNode node) {
        MctsNode current = node;
        while (!current.children.isEmpty()) {
            final int total = Math.max(1, current.visits);
            current = current.children.stream()
                .max(Comparator.comparingDouble(c -> c.ucb1(total, explorationConstant)))
                .orElse(current);
        }
        return current;
    }

    private MctsNode expand(MctsNode node, int nodeLimit) {
        if (lastExpansionCount >= nodeLimit) {
            return null;
        }
        List<GoapAction> actions = actionLibrary.applicableTo(node.state);
        if (actions.isEmpty()) {
            lastExpansionCount++;
            return node;
        }

        for (GoapAction action : actions) {
            if (lastExpansionCount >= nodeLimit) {
                break;
            }
            WorldState next = action.apply(node.state);
            node.children.add(new MctsNode(next, action, node));
            lastExpansionCount++;
        }

        if (node.children.isEmpty()) {
            return node;
        }

        return node.children.get(0);
    }

    private double simulate(MctsNode start,
                            GoapGoal goal,
                            Random random,
                            long deterministicSeed) {
        WorldState rolloutState = start.state;
        int depth = 0;
        while (depth < ROLLOUT_DEPTH && !goal.isSatisfied().test(rolloutState)) {
            List<GoapAction> actions = actionLibrary.applicableTo(rolloutState);
            if (actions.isEmpty()) {
                break;
            }
            long seed = deterministicSeed ^ depth ^ rolloutState.tick();
            random.setSeed(seed);
            GoapAction picked = actions.get(random.nextInt(actions.size()));
            rolloutState = picked.apply(rolloutState);
            depth++;
        }

        double reward = goal.isSatisfied().test(rolloutState) ? 1.0 : 0.0;
        return reward * Math.pow(0.9, depth);
    }

    private void backpropagate(MctsNode node, double reward) {
        MctsNode current = node;
        while (current != null) {
            current.visits++;
            current.totalReward += reward;
            current = current.parent;
        }
    }
}
