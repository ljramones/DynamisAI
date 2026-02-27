![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Modules](https://img.shields.io/badge/Modules-14-informational) ![Tests](https://img.shields.io/badge/Tests-800-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# DynamisAI

DynamisAI is a deterministic, multi-module AI stack for game simulation that combines perception, memory, planning, social reasoning, voice, and tooling under strict frame-budget and replayability constraints.

## What Is DynamisAI

DynamisAI is structured around one authoritative simulation timeline (`WorldSnapshot` + deterministic seeds) and a modular AI architecture where each subsystem can be swapped, tested, and profiled independently. The project is designed for high-NPC-count simulation while preserving debugability and strict runtime contracts.

Core doctrine:
- Authoritative AI is deterministic.
- LLM services assist cognition but do not own world truth.
- Every subsystem runs under explicit budget and degrade policy.
- Tooling (inspection, replay, simulation baking) ships with runtime.

## Architecture

### Three-Plane Model

| Plane | Responsibility | Typical Modules |
|---|---|---|
| Simulation Plane | Deterministic world state updates, scheduling, steering, gameplay-facing outputs. | `dynamis-ai-core`, `dynamis-ai-navigation`, `dynamis-ai-crowd`, `dynamis-ai-lod`, `dynamis-ai-planning` |
| Cognition Plane | Asynchronous inference, memory retrieval, dialogue generation, affect modeling. | `dynamis-ai-cognition`, `dynamis-ai-memory`, `dynamis-ai-voice`, `dynamis-ai-social` |
| Performance/Tools Plane | Debug tracing, replay, assertions, offline bake validation, extension contracts. | `dynamis-ai-tools`, `dynamis-ai-test-kit`, `dynamisai-extensions` |

### Cognitive Stack

| Layer | Input | Output | Key Module |
|---|---|---|---|
| Perception | `WorldSnapshot`, sound events, sensor profiles | `PerceptionSnapshot` | `dynamis-ai-perception` |
| Belief/Memory | Percepts + memory retrieval | `BeliefModel`, memory evidence | `dynamis-ai-cognition`, `dynamis-ai-memory` |
| Planning | `WorldState`, squad facts, influence maps | `Plan`, selected actions | `dynamis-ai-planning` |
| Social/Pacing | events, relationships, schedules, engagement metrics | modifiers, reputation deltas, rumor propagation | `dynamis-ai-social` |
| Execution/Output | plans + movement + voice/animation outputs | `AIOutputFrame` | `dynamis-ai-core`, `dynamis-ai-navigation`, `dynamis-ai-voice` |

### Module Dependency Sketch

```text
                           +----------------------+
                           |  dynamis-ai-core     |
                           +----------+-----------+
                                      |
          +---------------------------+----------------------------+
          |                           |                            |
+---------v----------+     +----------v----------+      +----------v----------+
| dynamis-ai-memory  |     | dynamis-ai-perception |    | dynamis-ai-planning |
+---------+----------+     +----------+----------+      +----------+----------+
          |                           |                            |
+---------v----------+     +----------v----------+      +----------v----------+
| dynamis-ai-cognition|     | dynamis-ai-crowd   |      | dynamis-ai-social   |
+---------+----------+     +----------+----------+      +----------+----------+
          |                           |                            |
+---------v----------+     +----------v----------+      +----------v----------+
| dynamis-ai-voice   |     | dynamis-ai-lod      |      | dynamis-ai-tools    |
+--------------------+     +---------------------+      +----------+----------+
                                                                     |
                                                          +----------v----------+
                                                          |  dynamis-ai-demo    |
                                                          +---------------------+
```

## Module Map

| Module | Purpose | Tests |
|---|---|---:|
| [`dynamis-ai-core`](./dynamis-ai-core/README.md) | Deterministic engine facade, snapshots, budget governor, adapters. | 51 |
| [`dynamis-ai-cognition`](./dynamis-ai-cognition/README.md) | Inference orchestration, affect, belief modeling, deterministic requests. | 67 |
| [`dynamis-ai-perception`](./dynamis-ai-perception/README.md) | Simulated senses, saliency, influence map engine (SIMD). | 64 |
| [`dynamis-ai-memory`](./dynamis-ai-memory/README.md) | Embeddings, vector stores, memory retrieval contracts. | 59 |
| [`dynamis-ai-navigation`](./dynamis-ai-navigation/README.md) | Pathfinding and steering output generation. | 30 |
| [`dynamis-ai-planning`](./dynamis-ai-planning/README.md) | HTN/GOAP/Utility/BT/MCTS planning and squad blackboard. | 115 |
| [`dynamis-ai-crowd`](./dynamis-ai-crowd/README.md) | Crowd movement, LOD-aware crowd simulation support. | 32 |
| [`dynamis-ai-voice`](./dynamis-ai-voice/README.md) | TTS pipeline, visemes, blendshape mapping outputs. | 82 |
| [`dynamis-ai-social`](./dynamis-ai-social/README.md) | Reputation, rumor propagation, schedule engine, drama manager. | 126 |
| [`dynamis-ai-tools`](./dynamis-ai-tools/README.md) | Debug snapshots, replay inspector, bake assertions and headless baking. | 91 |
| [`dynamis-ai-lod`](./dynamis-ai-lod/README.md) | AI LOD policy, importance evaluation, tick scaling. | 20 |
| [`dynamis-ai-demo`](./dynamis-ai-demo/README.md) | End-to-end demo harness integrating core subsystems. | 22 |
| [`dynamis-ai-test-kit`](./dynamis-ai-test-kit/README.md) | SPI contract tests for external implementations. | 7 |
| [`dynamisai-extensions`](./dynamisai-extensions/README.md) | Example SPI implementations for contributor onboarding. | 34 |

## Build And Run

### Prerequisites
- Java 25
- Maven 3.9+

### Full build

```bash
mvn clean install -DskipTests
```

### Full tests

```bash
mvn clean test
```

### Run demo module tests only

```bash
cd dynamis-ai-demo
mvn clean test
```

## Contributing

1. Pick a module and read its local README first.
2. Keep changes deterministic and budget-aware.
3. Add tests with every behavior change.
4. If adding or replacing an SPI implementation, validate against `dynamis-ai-test-kit` contract tests.
5. Prefer module-boundary-safe integrations (avoid introducing JPMS/reactor cycles).

## License

Licensed under the Apache License, Version 2.0.
See [LICENSE](./LICENSE) and [NOTICE](./NOTICE).
