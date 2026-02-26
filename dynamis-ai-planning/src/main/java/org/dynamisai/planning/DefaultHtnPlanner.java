package org.dynamisai.planning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultHtnPlanner implements HtnPlanner {

    private static final Logger log = LoggerFactory.getLogger(DefaultHtnPlanner.class);

    @Override
    public Plan plan(HtnTask rootTask, WorldState state, PlanningBudget budget) {
        long startNanos = System.nanoTime();
        PlanningContext ctx = new PlanningContext(budget, startNanos);

        List<HtnTask.PrimitiveTask> result = new ArrayList<>();
        DecomposeOutcome outcome = decompose(rootTask, state, result, 0, ctx);

        float totalCost = (float) result.stream().mapToDouble(HtnTask.PrimitiveTask::cost).sum();

        if (!ctx.complete) {
            log.debug("Planning incomplete for {} - nodeCount={} elapsed={}ms",
                rootTask.taskId(), ctx.nodeCount,
                (System.nanoTime() - startNanos) / 1_000_000);
        }

        return new Plan(List.copyOf(result), totalCost, ctx.maxDepthReached, ctx.complete);
    }

    private DecomposeOutcome decompose(HtnTask task,
                                       WorldState state,
                                       List<HtnTask.PrimitiveTask> result,
                                       int depth,
                                       PlanningContext ctx) {
        ctx.nodeCount++;
        ctx.maxDepthReached = Math.max(ctx.maxDepthReached, depth);

        if (ctx.nodeCount >= ctx.budget.maxNodes()) {
            ctx.complete = false;
            return DecomposeOutcome.failed(state);
        }
        if (System.nanoTime() - ctx.startNanos > ctx.budget.maxPlanningMs() * 1_000_000L) {
            ctx.complete = false;
            return DecomposeOutcome.failed(state);
        }
        if (depth >= ctx.budget.maxDepth()) {
            ctx.complete = false;
            return DecomposeOutcome.failed(state);
        }

        return switch (task) {
            case HtnTask.PrimitiveTask primitive -> {
                if (!primitive.canExecute(state)) {
                    yield DecomposeOutcome.failed(state);
                }
                result.add(primitive);
                yield DecomposeOutcome.success(applyEffects(state, primitive.effects()));
            }
            case HtnTask.CompoundTask compound -> {
                var method = DecompositionMethod.firstApplicable(compound.methods(), state);
                if (method.isEmpty()) {
                    log.debug("No applicable method for compound task '{}'", compound.taskId());
                    yield DecomposeOutcome.failed(state);
                }

                WorldState nextState = state;
                boolean success = true;
                for (HtnTask subtask : method.get().subtasks()) {
                    DecomposeOutcome sub = decompose(subtask, nextState, result, depth + 1, ctx);
                    if (!sub.success) {
                        success = false;
                        break;
                    }
                    nextState = sub.state;
                }
                yield success ? DecomposeOutcome.success(nextState) : DecomposeOutcome.failed(nextState);
            }
            case HtnTask.ConditionalTask conditional -> {
                HtnTask selected = conditional.evaluate(state);
                yield decompose(selected, state, result, depth + 1, ctx);
            }
        };
    }

    private WorldState applyEffects(WorldState state, List<PlannerEffect> effects) {
        if (effects.isEmpty()) {
            return state;
        }
        Map<String, Object> updated = new HashMap<>(state.blackboard());
        for (PlannerEffect effect : effects) {
            updated.put(effect.key(), effect.value());
        }
        return new WorldState(
            state.owner(), state.tick(), state.affect(),
            state.currentThreat(), state.perception(),
            state.memoryStats(), Map.copyOf(updated),
            state.agentPosition(), state.goalPosition(), state.distanceToGoal()
        );
    }

    private static final class PlanningContext {
        final PlanningBudget budget;
        final long startNanos;
        int nodeCount = 0;
        int maxDepthReached = 0;
        boolean complete = true;

        PlanningContext(PlanningBudget budget, long startNanos) {
            this.budget = budget;
            this.startNanos = startNanos;
        }
    }

    private record DecomposeOutcome(boolean success, WorldState state) {
        static DecomposeOutcome success(WorldState state) {
            return new DecomposeOutcome(true, state);
        }

        static DecomposeOutcome failed(WorldState state) {
            return new DecomposeOutcome(false, state);
        }
    }
}
