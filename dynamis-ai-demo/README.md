# dynamis-ai-demo

Interactive CLI demo for the DynamisAI stack.

This module is not a library. It is a runnable teaching artifact that shows how to wire the other modules together in one tick loop.

## Purpose

`dynamis-ai-demo` demonstrates how to integrate:

- `dynamis-ai-core` for world snapshots and frame budget scheduling
- `dynamis-ai-perception` for percept generation each tick
- `dynamis-ai-cognition` for async dialogue responses
- `dynamis-ai-memory` for memory lifecycle storage and consolidation
- `dynamis-ai-planning` semantics (task selection labels in the demo loop)
- `dynamis-ai-navigation` for async path requests and per-tick steering
- `dynamis-ai-social` for relationship state and dialogue shaping
- `dynamis-ai-crowd` for guard group movement/formation
- `dynamis-ai-voice` for TTS/animation bridge flow (mock pipeline in demo)
- `dynamis-ai-tools` as available integration surface in the full stack

## Scenario

Three entities:

- `Guard1` (`EntityId(1)`) at `(2,0,2)`
- `Guard2` (`EntityId(2)`) at `(4,0,2)`
- `Player` (`EntityId(3)`) at `(28,0,28)`

World/nav setup:

- NavMesh grid: `16 x 16`, `2m` cells
- Crowd group for guards in `LINE` formation
- Patrol waypoints:
  - `(2,0,2) -> (14,0,2) -> (14,0,14) -> (2,0,14) -> repeat`

## Controls

Each tick, input one action:

- `A` approach
- `H` hostile act
- `F` flee
- `W` wait
- `S` speak (free text prompt)

## Run

From repo root, install dependencies first:

```bash
for dir in dynamis-ai-core dynamis-ai-cognition dynamis-ai-perception \
           dynamis-ai-memory dynamis-ai-planning dynamis-ai-navigation \
           dynamis-ai-voice dynamis-ai-social dynamis-ai-crowd dynamis-ai-tools; do
  cd $dir && mvn clean install -DskipTests && cd ..
done
```

Then run the demo:

```bash
cd dynamis-ai-demo
mvn clean test
mvn exec:java
```

At exit, JSON output is written to:

- `dynamis-ai-demo/demo-report.json`

## What Happens Each Tick

`DemoWorld.tick(...)` orchestrates the loop:

1. Apply player action (`PlayerAction`)
2. Push entity states into `DefaultWorldStateStore`
3. Advance patrol goal if waypoint reached
4. Run `DefaultBudgetGovernor` frame tasks
   - crowd tick
   - memory consolidate
   - navigation steer
5. Run perception for Guard1
6. Derive task labels (patrol/approach/dialogue/flee/callBackup)
7. If in dialogue range:
   - build `DialogueRequest`
   - shape with `SocialDialogueShaper`
   - call `DefaultCognitionService.requestDialogue(...)`
   - record dialogue in `DefaultSocialSystem`
   - add memory event to `DefaultMemoryLifecycleManager`
   - send response through `MockTTSPipeline` -> `DefaultAnimisBridge`
8. Sync guard positions from `CrowdSnapshot`
9. Determine scenario outcome
10. Return `TickRecord` and append to `DemoReport`

## Class Map

- `DynamisAiDemo`
  - CLI entrypoint and game loop (`MAX_TICKS=20`)
- `DemoWorld`
  - owns all system instances and the tick orchestration
- `DemoCli`
  - terminal rendering and input capture
- `DemoNpc`
  - mutable scenario entity state
- `PlayerAction`
  - input enum and parser
- `TickRecord`
  - immutable per-tick summary DTO
- `DemoReport`
  - accumulates ticks and writes final JSON
- `DemoVectorMemoryStore`
  - demo-safe `VectorMemoryStore` adapter used by memory lifecycle manager

## Why `DemoVectorMemoryStore` Exists

The memory module contains incubator-vector/off-heap implementations intended for real runtime environments with explicit JVM flags.

For this CLI teaching module, `DemoVectorMemoryStore` keeps behavior simple and async while avoiding runtime friction in `mvn exec:java`. It still exercises memory lifecycle manager flow and `findSimilar()` async semantics.

## Teaching Notes: How Modules Are Used Together

### Core + Perception

- Core supplies authoritative state (`WorldStateStore` snapshots)
- Perception queries that state and emits `PerceptionSnapshot`

### Social + Cognition

- `SocialDialogueShaper.shape(...)` transforms a cognition request without introducing cognition->social dependency
- Relationship history influences mood and dialogue hint prefixes

### Navigation + Crowd

- Crowd controls group-level formation and LOD
- Navigation handles per-agent path/steering concerns
- Demo synchronizes guard positions from crowd snapshot each tick

### Memory + Dialogue

- Dialogue events become `MemoryRecord.rawEvent(...)`
- Memory manager consolidates records over time on budgeted tasks

### Voice + Animation Bridge

- Dialogue response is rendered by `MockTTSPipeline`
- Voice job is submitted to `DefaultAnimisBridge`

## Extending the Demo

Common next changes:

1. Replace `MockInferenceBackend` with a real backend in `DemoWorld`
2. Replace `MockTTSPipeline` with production TTS in `DemoWorld`
3. Add more entities and a second crowd group
4. Integrate real HTN decomposition execution from `dynamis-ai-planning`
5. Add player faction changes and branch dialogue by standing
6. Emit telemetry per tick from `TickRecord.systemsActive`

## Testing

Demo tests are in:

- `src/test/java/org/dynamisai/demo/DemoWorldTest.java`

They verify key behavior:

- player movement (`APPROACH`, `FLEE`, `WAIT`)
- hostile social tagging (`ENEMY`, `BETRAYED`)
- tick record/report output
- world initialization and crowd membership

Run:

```bash
cd dynamis-ai-demo
mvn clean test
```
