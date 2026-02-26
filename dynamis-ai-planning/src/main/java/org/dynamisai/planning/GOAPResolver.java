package org.dynamisai.planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/** Dijkstra-style GOAP resolver (A* with zero heuristic). */
public final class GOAPResolver {

    private final GoapActionLibrary actionLibrary;

    public GOAPResolver(GoapActionLibrary actionLibrary) {
        this.actionLibrary = actionLibrary;
    }

    public Optional<Plan> resolve(GoapGoal goal,
                                  WorldState currentState,
                                  PlanningBudget budget) {
        long startNanos = System.nanoTime();

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::cost));
        Set<String> visited = new HashSet<>();
        Node start = new Node(currentState, List.of(), 0f);
        Node bestPartial = start;
        int expanded = 0;

        open.add(start);

        while (!open.isEmpty()) {
            if (expanded >= budget.maxNodes() || elapsedMs(startNanos) > budget.maxPlanningMs()) {
                return Optional.of(toPartialPlan(bestPartial));
            }

            Node node = open.poll();
            expanded++;
            if (node.cost() < bestPartial.cost()) {
                bestPartial = node;
            }

            String key = stateKey(node.state());
            if (!visited.add(key)) {
                continue;
            }

            if (goal.isSatisfied().test(node.state())) {
                return Optional.of(toCompletePlan(node));
            }

            for (GoapAction action : actionLibrary.applicableTo(node.state())) {
                WorldState next = action.apply(node.state());
                List<GoapAction> nextActions = new ArrayList<>(node.actions());
                nextActions.add(action);
                float nextCost = node.cost() + action.cost(node.state());
                Node child = new Node(next, List.copyOf(nextActions), nextCost);
                if (child.cost() < bestPartial.cost() || bestPartial.actions().isEmpty()) {
                    bestPartial = child;
                }
                open.add(child);
            }
        }

        return Optional.empty();
    }

    public Optional<Plan> resolveHighestPriority(List<GoapGoal> goals,
                                                 WorldState currentState,
                                                 PlanningBudget budget) {
        return goals.stream()
            .sorted(Comparator.comparingDouble(GoapGoal::priority).reversed())
            .map(goal -> resolve(goal, currentState, budget))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static String stateKey(WorldState state) {
        TreeMap<String, String> normalized = new TreeMap<>();
        for (Map.Entry<String, Object> entry : state.blackboard().entrySet()) {
            normalized.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return normalized.toString();
    }

    private static Plan toCompletePlan(Node node) {
        return toPlan(node, true);
    }

    private static Plan toPartialPlan(Node node) {
        return toPlan(node, false);
    }

    private static Plan toPlan(Node node, boolean complete) {
        List<HtnTask.PrimitiveTask> tasks = node.actions().stream()
            .map(a -> new HtnTask.PrimitiveTask(
                a.name(),
                a.name(),
                a.precondition(),
                a.effects(),
                a.baseCost(),
                () -> {}
            ))
            .toList();
        return new Plan(tasks, node.cost(), tasks.size(), complete);
    }

    private record Node(WorldState state, List<GoapAction> actions, float cost) {}
}
