![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-115-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-planning

## TLDR
`dynamis-ai-planning` is the decision layer that turns state into actionable NPC plans. It includes HTN decomposition, GOAP search, utility scoring, behavior tree runtime, MCTS action selection, and squad blackboard propagation. Without this module, behaviors are static scripts with limited adaptation. With it, agents can share tactical facts, choose goals dynamically, and execute multi-step plans under budget constraints.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-planning</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `HtnPlanner` / `DefaultHtnPlanner` | Hierarchical task decomposition. | Uses `HtnTask` sealed hierarchy and `DecompositionMethod` rules. |
| `GOAPResolver` | Goal-oriented action planning. | Cost-aware search over `GoapAction` graph and `WorldState`. |
| `GoapActionLibrary` | Action registry for GOAP/MCTS. | Query applicable actions from current state. |
| `UtilityEvaluator` | Utility scoring and winner selection. | Executes callback on selected action. |
| `BehaviorTree` | Runtime BT execution wrapper. | Ticks `BtNode` trees with deterministic context. |
| `MCTSPlanner` | Monte Carlo action recommendation. | Seeded rollout for reproducible choices. |
| `SquadBlackboard` | Shared tactical fact store with propagation delay. | Channels: radio, line-of-sight, runner. |
| `SquadBlackboardRegistry` | Squad board lifecycle management. | Create/disband/prune per squad. |
| `WorldState` | Planning-state container. | Immutable map + helper methods + squad fact merge. |
| `Plan` | Executable plan payload. | Holds selected tasks and estimated cost metadata. |
| `PlanningBudget` | Search budget constraints. | Limits nodes/depth/time for resolvers. |
| `CommonBehaviorTrees` / `CommonGoapActions` | Reusable authored patterns. | Baseline guard/combat/flee behavior templates. |

### Code Examples
// Scenario: decompose and execute an HTN plan candidate.
```java
import org.dynamisengine.ai.planning.*;

HtnPlanner planner = new DefaultHtnPlanner();
WorldState state = new WorldState(java.util.Map.of("threat", "low"));
HtnTask root = TaskLibrary.guardPatrolTask();
Plan plan = planner.plan(root, state, PlanningBudget.defaultBudget()).orElseThrow();
System.out.println(plan.tasks().size());
```

// Scenario: resolve GOAP plan from dynamic goals.
```java
import org.dynamisengine.ai.planning.*;

GoapActionLibrary actions = new GoapActionLibrary();
actions.register(CommonGoapActions.moveTo());
actions.register(CommonGoapActions.requestBackup());

GOAPResolver resolver = new GOAPResolver(actions);
GoapGoal goal = new GoapGoal("backup", s -> s.has(SquadFacts.BACKUP_REQUESTED), 0.8f);
WorldState state = new WorldState(java.util.Map.of());
resolver.resolve(goal, state, PlanningBudget.defaultBudget());
```

// Scenario: share tactical facts via squad blackboard propagation rules.
```java
import org.dynamisengine.ai.planning.*;
import org.dynamisengine.ai.core.*;

SquadBlackboard board = new SquadBlackboard("alpha");
EntityId leader = EntityId.of(1);
board.writeRadio(SquadFacts.THREAT_POSITION, new Location(10,0,5), leader, 100L);

WorldState local = new WorldState(java.util.Map.of());
WorldState enriched = local.withSquadFacts(board, EntityId.of(2), new Location(0,0,0), new Location(0,0,0), 100L);
System.out.println(enriched.has(SquadFacts.THREAT_POSITION));
```

// Depends on sibling module: `dynamis-ai-perception`
// Scenario: consult influence-map output to choose low-threat cover.
```java
import org.dynamisengine.ai.planning.*;
import org.dynamisengine.ai.perception.*;
import org.dynamisengine.ai.core.Location;

InfluenceMapSnapshot map = /* obtained from perception module */ null;
Location cover = map.lowestCellNear(InfluenceLayer.THREAT, new Location(5,0,5), 15f);
WorldState s = new WorldState(java.util.Map.of("cover.available", true, "target.position", cover));
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `HtnPlanner` | HTN decomposition strategy/runtime. | No dedicated contract class yet |

### Related Modules
- `dynamis-ai-core` — provides world identifiers, locations, and budget integration points.
- `dynamis-ai-perception` — supplies influence maps and sensory context used by tactical action scoring.
- `dynamis-ai-navigation` — executes movement-oriented planning operators.
- `dynamis-ai-social` — consumes squad facts and planner outputs for social/tactical coordination.

## Internals and Porting Guide

### Architecture
Planning is intentionally multi-paradigm. HTN handles authored structure, GOAP handles emergent search, utility scoring ranks immediate options, BT handles reactive authored control flow, and MCTS provides bounded stochastic lookahead. All of these operate over `WorldState` so the decision surface stays consistent.

`WorldState` is immutable and key-value oriented, which keeps transformations cheap and composable. `PlannerEffect` expresses blackboard mutation as data. Resolvers and runtime nodes return new state/plan objects rather than mutating caller-owned maps, enabling deterministic replay and branch evaluation.

Squad coordination is separate from local state through `SquadBlackboard`. Facts are written once and become readable per propagation channel rules. `WorldState.withSquadFacts(...)` is the explicit merge boundary, so planning logic consumes a coherent local+squad snapshot without direct mutable shared access.

Behavior tree runtime is data-first. `BtNode` is a sealed hierarchy with composite/decorator/leaf nodes. `BehaviorTree.tick` handles reset semantics after terminal statuses while preserving `RUNNING` continuation state. This enables authored trees without a visual runtime dependency.

MCTS and GOAP both consume `GoapActionLibrary`. GOAP resolves full plans; MCTS recommends robust next actions under rollout budget. Seeded randomness in MCTS keeps repeated runs reproducible under the same deterministic context.

### Key Design Decisions
1. Multiple planning paradigms coexist instead of a single planner.
Reason: authored story behavior and emergent tactical adaptation require different control abstractions.
2. `WorldState` remains immutable.
Reason: branch-heavy search (GOAP/MCTS/HTN) needs safe copy-on-write semantics.
3. Squad knowledge is explicit merge (`withSquadFacts`) rather than global mutable singleton reads.
Reason: preserves propagation realism and deterministic local planning inputs.
4. BT runtime ships code-first without editor coupling.
Reason: keeps module portable and testable in headless CI.

### Threading and Lifecycle
Planning objects are mostly stateless per invocation (`GOAPResolver`, `DefaultHtnPlanner`), while registries/libraries (`GoapActionLibrary`, `SquadBlackboardRegistry`) are mutable shared state and should be owned by simulation services. Tick-facing runtime (`BehaviorTree`) should run on simulation thread per agent/context. MCTS/GOAP budgets should be aligned with performance-plane frame policies.

### Porting Notes
This module relies on records, sealed hierarchies, immutable maps, and deterministic pseudo-random rollouts. Porters should map these to ADTs/data classes and persistent maps. Replace Java lambdas (`Predicate`, `Function`, `Runnable`) with first-class function types in target languages. Keep deterministic seed usage and explicit budget arguments intact to preserve replayability.

### Known Limitations and Gotchas
- `WorldState` stores `Object` values; key conventions must stay consistent across modules.
- GOAP and MCTS output quality depends on action-library completeness and effect coherence.
- BT `RUNNING` state is node-local; reusing one tree instance across unrelated agents can leak progress unless isolated/reset.
- Runner/LOS propagation in squad blackboard needs correct position inputs at read time.
- Planning budgets that are too small can return partial/empty plans by design.
