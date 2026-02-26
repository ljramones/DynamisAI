# DynamisAI

DynamisAI is the cognitive architecture layer for a Java AAA engine stack. Its goal is to produce believable NPC behavior that is deterministic, auditable, and scalable, while keeping expressive dialogue and voice generation safely bounded behind strict runtime contracts.

## Vision

The core behavior chain is:

`Perception -> Memory -> Belief -> Intent -> Action -> Voice/Animation`

Every link is grounded in a single authoritative world snapshot so AI decisions stay coherent across systems.

## Non-Negotiable Rules

1. Deterministic simulation logic is authoritative.
2. LLM is a service, not the decision core.
3. Every subsystem has a budget, degradation policy, and fallback.
4. Generative output is schema-constrained (typed JSON), never raw game logic.
5. Tooling (inspection, traceability, replay) ships with runtime.
6. Snapshot storage uses structural sharing (no deep-copy snapshots).
7. LLM output is never executed as runtime behavior logic.

## Architecture at a Glance

### Three Planes

- **Simulation Plane**: deterministic gameplay AI (perception, planning, navigation, behavior).
- **Cognition Plane**: async inference and TTS work via bounded virtual-thread services.
- **Performance Plane**: budget governance, LOD policy, caching, and graceful degradation.

### Core Build Order

Build these foundations before feature work:

1. `WorldStateStore`
2. `BudgetGovernor`
3. `InferenceBackend`
4. `CognitionService`

## Repository Status

This repository is currently specification-first:

- `docs/DynamisAI_Master_Spec.docx`
- `docs/DynamisAI_Spec_v1.1.docx`
- `docs/DynamisAI_Architecture_NextSteps.docx`
- `.java-version` (JDK `25`)

## Planned Package Layout

When implementation begins, use:

- `src/main/java/com/dynamis/ai/core`
- `src/main/java/com/dynamis/ai/cognition`
- `src/main/java/com/dynamis/ai/planning`
- `src/main/java/com/dynamis/ai/perception`
- `src/main/java/com/dynamis/ai/navigation`
- `src/main/java/com/dynamis/ai/voice`
- `src/main/java/com/dynamis/ai/lod`
- `src/test/java/...` (mirrored package structure)

## Getting Started (Current)

```bash
java -version
cat .java-version
ls docs
```

## Next Implementation Steps

1. Initialize Java project structure and build tool wrapper.
2. Implement immutable snapshot contracts in `WorldStateStore`.
3. Add `BudgetGovernor` frame scheduler with ordered degradation ladder.
4. Introduce pluggable `InferenceBackend` interface and deterministic config.
5. Add bounded async `CognitionService` with hard timeouts and fallback behavior.

## Milestones (First 4 Weeks)

### Week 1: Project Bootstrap + Core Contracts

- Create Gradle or Maven wrapper and baseline CI checks.
- Scaffold `com.dynamis.ai.core` with interfaces/records only.
- Define deterministic seed utility and shared error/result types.
- Deliverable: compiles clean with placeholder unit tests.

### Week 2: WorldStateStore (Authoritative Snapshot Layer)

- Implement immutable `WorldSnapshot` and `WorldChange` pipeline.
- Add structural-sharing persistence strategy (no deep copies).
- Add scoped query surface (`QueryScope`) and tick commit flow.
- Deliverable: deterministic replay test for snapshot history.

### Week 3: BudgetGovernor (Frame Budget Control)

- Implement `AITaskNode` registry, priority scheduling, and frame execution.
- Implement ordered degradation ladder thresholds and fallback invocation.
- Expose frame report metrics for inspector integration.
- Deliverable: load test showing graceful degradation without frame overrun.

### Week 4: InferenceBackend + CognitionService

- Add pluggable `InferenceBackend` contract and deterministic generation config.
- Implement `CognitionService` with bounded virtual-thread concurrency.
- Enforce timeout, cancellation, cache path, and mandatory fallback response.
- Deliverable: async cognition tests proving non-blocking simulation behavior.

## Source of Truth

For architecture and constraints, treat the docs in `docs/` as canonical until code-level ADRs and APIs are published.
