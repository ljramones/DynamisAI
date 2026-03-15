![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-51-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-core

## TLDR
`dynamis-ai-core` defines the deterministic simulation contract used by every other DynamisAI module. It owns world snapshots, world mutation queues, frame budgeting, and the `DynamisAiEngine` facade that adapters tick each frame. Without this module, each subsystem would invent incompatible timing and state models. With it, all modules share one authoritative world timeline and one budget/degradation pipeline.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `DynamisAiEngine` | Main facade for game-loop integration. | Mutable runtime object; tick from one thread. |
| `GameEngineAdapter` | SPI for engine bindings. | Implement in engine module; call `initialize/tick/shutdown`. |
| `HeadlessGameEngineAdapter` | Minimal adapter for tests and offline runs. | No rendering/audio integration. |
| `WorldStateStore` | Contract for queued world writes and tick commits. | `DefaultWorldStateStore` provides snapshot history ring buffer. |
| `WorldSnapshot` | Immutable world-at-tick record. | Carries deterministic seed derived from tick. |
| `WorldChange` | Sealed mutation envelope. | Supports entity, fact, environment, relationship, narrative-rails changes. |
| `BudgetGovernor` | Frame budget scheduler contract. | `DefaultBudgetGovernor` applies priority + degrade mode policy. |
| `FrameBudgetReport` | Per-frame execution report. | Use for profiling and QoS inspection. |
| `AIOutputFrame` | Per-tick output payload for adapters. | Immutable result: steering/dialogue/animation + snapshot + budget report. |
| `DeterminismSeedManager` | Seed derivation utility. | Pure static hashing for snapshot/NPC seeds. |
| `EntityState` | Authoritative state for one entity. | Position plus arbitrary property map. |
| `LodTier` | Shared LOD tier enum for cross-module scheduling. | Reused by lod/crowd/core integrations. |

### Code Examples
// Scenario: create and tick a headless engine frame.
```java
import org.dynamisengine.ai.core.*;

DynamisAiEngine engine = DynamisAiEngine.builder().build();
HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
adapter.initialize(engine);

GameEngineContext ctx = GameEngineContext.builder(1L, 1f / 60f)
    .observer(new Location(0, 0, 0))
    .build();

AIOutputFrame frame = adapter.tick(ctx);
System.out.println(frame.tick());
```

// Scenario: queue world changes then commit one deterministic snapshot tick.
```java
import org.dynamisengine.ai.core.*;

DefaultWorldStateStore store = new DefaultWorldStateStore();
EntityId npc = EntityId.of(10L);
store.enqueueChange(new WorldChange.EntityStateChange(
    npc,
    new EntityState(npc, new Location(5, 0, 3), java.util.Map.of("alert", true))
));
store.enqueueChange(new WorldChange.FactChange("weather", "rain"));
store.commitTick();

WorldSnapshot s = store.getCurrentSnapshot();
System.out.println(s.tick() + " seed=" + s.deterministicSeed());
```

// Scenario: register budgeted tasks with fallback behavior.
```java
import org.dynamisengine.ai.core.*;

DefaultBudgetGovernor governor = new DefaultBudgetGovernor(16);
governor.register(new AITaskNode(
    "perception",
    4,
    Priority.HIGH,
    DegradeMode.FALLBACK,
    () -> { /* run full quality */ },
    () -> { /* run fallback quality */ }
));

governor.runFrame(42L, new DefaultWorldStateStore().getCurrentSnapshot());
FrameBudgetReport report = governor.getLastFrameReport();
System.out.println(report.tick());
```

// Depends on sibling module: `dynamis-ai-tools`
// Scenario: attach tools inspector without creating a core->tools compile dependency.
```java
import org.dynamisengine.ai.core.*;
import org.dynamisengine.ai.tools.AIInspector;

DynamisAiEngine engine = DynamisAiEngine.builder().build();
AIInspector inspector = new AIInspector();
engine.attachInspector(inspector); // reflective hook in core

HeadlessGameEngineAdapter adapter = new HeadlessGameEngineAdapter();
adapter.initialize(engine);
adapter.tick(GameEngineContext.builder(1L, 1f / 60f).build());

System.out.println(inspector.snapshotStore().size());
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `GameEngineAdapter` | Engine-specific game-loop binding. | `dynamis-ai-test-kit` → `GameEngineAdapterContractTest` |

### Related Modules
- `dynamis-ai-cognition` — consumes `WorldFacts`, `EntityId`, and deterministic seed context for inference orchestration.
- `dynamis-ai-planning` — uses `WorldSnapshot`, `LodTier`, and budget cadence to resolve plans.
- `dynamis-ai-tools` — attaches inspector/bake tooling to `DynamisAiEngine`.

## Internals and Porting Guide

### Architecture
Core follows an immutable-snapshot plus queued-delta model. Writers enqueue `WorldChange` objects, and `DefaultWorldStateStore.commitTick()` applies all queued changes atomically into the next `WorldSnapshot`. Readers consume snapshot state without locks because the snapshot is immutable.

`DynamisAiEngine` is the runtime facade that adapters hold. A tick applies adapter-provided changes, commits world state, runs the budget governor, then returns an immutable `AIOutputFrame`. This keeps simulation sequencing stable and observable across integrations.

Budgeting is centralized in `DefaultBudgetGovernor`. Tasks register static metadata (`Priority`, budget estimate, `DegradeMode`) and lambdas for full/fallback execution. A frame run records executed/skipped/degraded tasks in `FrameBudgetReport`, which becomes part of the output frame.

Determinism is explicit. `WorldSnapshot` carries a snapshot seed derived from tick, and per-entity seeds are derived from `(EntityId,tick)` via `DeterminismSeedManager`. This gives reproducible simulation decisions and replay behavior.

### Key Design Decisions
1. `WorldSnapshot` uses Vavr persistent `HashMap`.
Reason: structural sharing keeps per-tick snapshot churn bounded while preserving immutable read semantics.
2. Core does not compile against tools for inspector wiring.
Reason: reflective hook avoids a JPMS/reacctor cycle while keeping optional observability.
3. `WorldChange` is a sealed hierarchy.
Reason: mutation types stay explicit and exhaustively handled during commit.
4. Budgeting is centralized instead of per-subsystem local schedulers.
Reason: one frame report and one degrade policy surface simplifies profiling and predictable QoS.

### Threading and Lifecycle
`GameEngineAdapter.initialize()` and `shutdown()` run on engine lifecycle thread. `GameEngineAdapter.tick()` should run on one game-loop thread; `DynamisAiEngine.tick()` is not multi-thread safe. `DefaultWorldStateStore.enqueueChange()` is thread-safe, while `commitTick()` is simulation-thread-only. `AIOutputFrame` and `WorldSnapshot` are immutable and safe to share across threads after tick return.

### Porting Notes
This module uses Vavr persistent collections, Java records, sealed interfaces, and JPMS module boundaries. Replace Vavr with persistent maps in the target runtime, replace records/sealed types with algebraic data types or equivalent, and enforce module boundary rules in your package system. `DefaultBudgetGovernor` logic ports directly to any runtime with monotonic clock and priority queues.

### Known Limitations and Gotchas
- `DynamisAiEngine.tick()` assumes single-threaded access; concurrent ticks can race mutable accumulators.
- `attachInspector` uses reflection; signature drift on tools-side `record(WorldSnapshot,Map)` disables inspector capture.
- `WorldChange.RelationshipChange` writes into entity property maps by generated string keys; consumers must agree on key conventions.
- `FrameBudgetReport` can be empty if no tasks are registered, which is valid runtime behavior.
