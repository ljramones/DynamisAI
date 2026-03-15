# DynamisAI Architecture Boundary Ratification Review

Date: 2026-03-11

## 1. Intent and Scope

DynamisAI should be the subsystem that owns AI cognition and decision policy, not simulation or world authority.

DynamisAI should own:
- decision-making policy (goal selection, action scoring, tactical choice)
- planning systems (HTN/GOAP/BT/utility/MCTS logic)
- perception interpretation (sensor filtering, saliency, threat interpretation)
- behavior logic and social reasoning
- AI-local blackboard/memory (agent and squad cognition state)
- navigation interfaces and movement intent derivation
- intent generation/output contracts (steering/dialogue/animation intent signals)

DynamisAI must not own:
- authoritative world mutation
- simulation execution authority
- world/runtime orchestration authority

## 2. Repo Overview (Grounded)

Repository shape is a multi-module Maven project with 14 modules in `DynamisAI/pom.xml`.

Major packages and runtime surfaces:
- `dynamis-ai-core` (`org.dynamisengine.ai.core`): `DynamisAiEngine`, `DefaultBudgetGovernor`, `AIOutputFrame`, `WorldStateStore`, `DefaultWorldStateStore`, `WorldSnapshot`, `WorldChange`, `GameEngineContext`.
- `dynamis-ai-perception` (`org.dynamisengine.ai.perception`): `PerceptionSystem`, `DefaultPerceptionSystem`, saliency and influence-map surfaces.
- `dynamis-ai-cognition` (`org.dynamisengine.ai.cognition`): async `CognitionService`, `DefaultCognitionService`, belief models and inference backends.
- `dynamis-ai-planning` (`org.dynamisengine.ai.planning`): `DefaultHtnPlanner`, `GOAPResolver`, BT/utility/HTN types, `SquadBlackboard`, `PlanToIntentAdapter`.
- `dynamis-ai-navigation` (`org.dynamisengine.ai.navigation`): `NavigationSystem`, pathfinding/steering/RVO, plus `MovementIntegrator`.
- `dynamis-ai-social` (`org.dynamisengine.ai.social`): relationship/reputation/scheduling/drama systems.
- `dynamis-ai-crowd` (`org.dynamisengine.ai.crowd`): crowd grouping, LOD, formation simulation, `CrowdSnapshot`.
- `dynamis-ai-voice` (`org.dynamisengine.ai.voice`): TTS surfaces and `AnimisBridge` contract.
- `dynamis-ai-tools` (`org.dynamisengine.ai.tools`): wiring, replay, simulation baking, scripting/event buses via API contracts.

Notable abstractions:
- AI outputs are exposed as immutable `AIOutputFrame` with steering/dialogue/animation outputs.
- Planning has explicit intent adaptation (`PlanToIntentAdapter`) to scripting `Intent`.
- All modules export their package wholesale via JPMS `exports`, with no public/internal split inside each module.

## 3. Strict Ownership Statement

DynamisAI should exclusively own:
- deterministic AI decision policy and frame-budgeted AI task scheduling
- AI planning and tactical reasoning models (HTN/GOAP/BT/utility/MCTS)
- perception-to-belief interpretation and saliency modeling
- social/relationship reasoning and drama pacing logic
- AI-local cognitive state:
  - belief graphs
  - memory embeddings/retrieval
  - squad blackboards
- navigation reasoning and steering intent derivation (not authoritative transform writes)
- intent/event generation toward other executors:
  - movement intent/steering outputs
  - dialogue events
  - animation/voice intent signals

## 4. Explicit Non-Ownership

DynamisAI must not own:
- world lifecycle orchestration (WorldEngine authority)
- authoritative world-state mutation and commit semantics
- physics stepping or simulation authority
- collision/query substrate authority
- animation system authority (Animus owns animation execution)
- rendering authority (LightEngine/GPU boundary)
- input authority
- scripting execution authority
- persistence/session authority
- global event bus ownership

## 5. Dependency Rules

Allowed dependency patterns for DynamisAI:
- ECS/world read/query access through bounded interfaces/snapshots
- event consumption/emission through public API buses/contracts
- content/config definitions for behavior/perception/planning
- navigation query interfaces and pathfinding internals
- animation request interfaces (for intent signaling)
- scripting API contracts for intent/percept exchange (not runtime ownership)

Forbidden dependency patterns for DynamisAI:
- direct world orchestration ownership
- direct authoritative mutation/commit of simulation-owned state
- direct physics ownership or stepping
- direct SceneGraph ownership
- direct rendering/GPU dependencies
- direct scripting runtime execution ownership
- persistence/session ownership

Repo-grounded notes:
- Current imports show no direct dependencies on DynamisWorldEngine, DynamisECS, DynamisPhysics, DynamisCollision, DynamisSceneGraph, or DynamisLightEngine modules.
- `dynamis-ai-tools` depends on scripting/audio APIs, which is boundary-safe if kept as adapter/tooling and not execution ownership.

## 6. Public vs Internal Boundary Assessment

Current public/internal boundary is not clean.

Findings:
- Each module exports a single broad package (`exports org.dynamisengine.ai.*`) containing both contract and implementation classes.
- Internal implementation types are externally consumable by default (for example `Default*` implementations and integration helpers).
- `dynamis-ai-navigation` publicly exposes `MovementIntegrator`, which performs state integration/mutation behavior and should be treated as integration-internal.
- `dynamis-ai-tools` contains runtime wiring/orchestration surfaces that are useful, but public exposure is broad and unconstrained.

Assessment: API/internal split exists at module granularity, but not within modules; this increases implementation leakage risk.

## 7. Authority Leakage or Overlap

Major leakage is present.

Confirmed overlap/intrusion:
- WorldEngine overlap:
  - `dynamis-ai-core` owns `WorldStateStore`, `WorldSnapshot`, `WorldChange`, and `commitTick` mechanics.
  - `DynamisAiEngine.tick(...)` enqueues entity/world changes and commits snapshots.
  - This is world-state authority behavior, not purely decision intent.
- Task execution vs intent emission:
  - `DefaultBudgetGovernor` executes runnable tasks directly each frame.
  - This is valid for AI-internal scheduling, but becomes leakage when tasks perform authoritative world mutation.
- Navigation execution boundary leak:
  - `MovementIntegrator` writes entity state into `WorldStateStore` and calls `commitTick()`.
  - This crosses from decision/navigation into simulation execution.
- Scripting boundary status:
  - Planning has explicit intent adaptation (`PlanToIntentAdapter`) and tools emit `IntentBus` events, which is boundary-correct.
  - No direct scripting runtime ownership found in core AI modules.
- Animus/animation boundary status:
  - `AnimisBridge` is intent/event based and does not directly own animation execution.

No direct overlap found with Physics, Collision, SceneGraph, or rendering modules via dependencies/imports.

Critical distinction:
- AI deciding/intending is present and strong.
- AI directly mutating/committing world state is also present and is the main boundary violation.

## 8. Relationship Clarification

DynamisAI should relate to adjacent subsystems as follows:

- WorldEngine:
  - DynamisAI should consume world snapshots/query surfaces from WorldEngine.
  - DynamisAI should emit AI intents/effects for WorldEngine (or execution layer) to apply.
  - DynamisAI should not own authoritative world snapshot commit lifecycle.

- ECS:
  - DynamisAI should consume ECS-derived read models/adapters.
  - DynamisAI should emit intent or component-change requests through an execution boundary.
  - DynamisAI should not own ECS storage or archetype lifecycle.

- Physics:
  - DynamisAI should consume physics-derived observations (threat, LOS, movement constraints).
  - DynamisAI should emit movement/action intents.
  - DynamisAI should not step physics or write authoritative physics state.

- Collision:
  - DynamisAI should consume collision/query results via interfaces.
  - DynamisAI should not own collision substrate or query infrastructure lifecycle.

- Animus:
  - DynamisAI should emit animation-affect/intent signals.
  - DynamisAI should not own rig evaluation, blend execution, or animation timing authority.

- Scripting:
  - DynamisAI should consume percept/world events and emit intents via scripting API contracts.
  - DynamisAI should not own scripting runtime execution/orchestration.

- Event:
  - DynamisAI should consume and emit domain events through bounded contracts.
  - DynamisAI should not own global event transport runtime.

- SceneGraph:
  - DynamisAI should consume scene/spatial read models.
  - DynamisAI should not own scene node authority or hierarchy mutation.

- Content:
  - DynamisAI should consume authored behavior/config/content definitions.
  - DynamisAI should not own canonical content lifecycle/pipeline.

## 9. Ratification Result

**Boundary requires architectural correction**.

Justification:
- The repository has strong decision/perception/planning structure and clear intent surfaces.
- However, core modules currently include direct world-state ownership and commit authority (`WorldStateStore`/`commitTick`, `WorldChange`, `MovementIntegrator`), which crosses into simulation/runtime authority that should reside outside DynamisAI under the established Dynamis architecture.

## 10. Boundary Rules Going Forward

- DynamisAI must emit intent and AI outputs; it must not own authoritative simulation commit.
- DynamisAI must consume world/query data via bounded interfaces provided by world/simulation authority.
- DynamisAI must not become a shadow orchestrator for world lifecycle or global tick commit.
- AI-local memory/blackboards must remain non-authoritative and distinct from canonical world state.
- Navigation and crowd systems must produce movement intent/steering outputs; transform application belongs to execution authority outside DynamisAI.
- Scripting integration must stay API-contract based (intent/percept exchange), not runtime ownership.
- Animation integration must stay signal/bridge based; execution authority remains with Animus.
- Public API should be narrowed to contract surfaces; implementation helpers/integration mutators should be internalized.
- If repository-level demo tooling requires local simulation stores, they must be explicitly marked non-authoritative and isolated from production authority boundaries.
