![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-30-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-navigation

## TLDR
`dynamis-ai-navigation` computes where NPCs should move and how they steer each tick. It combines navmesh pathing, hierarchical graph acceleration, and local avoidance helpers under one `NavigationSystem` interface. Without this module, planners can choose goals but cannot realize movement safely. With it, goal positions become executable steering output in deterministic simulation time.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-navigation</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `NavigationSystem` | Stable movement/path query SPI. | Async path requests + per-tick steering. |
| `DefaultNavigationSystem` | Main implementation. | Works over `NavMesh` and optional HPA acceleration. |
| `PathRequest` | Path query input envelope. | Agent, start, goal, constraints. |
| `PathResult` | Sealed async path result. | Success/failure payload variants. |
| `NavMesh` | Traversable polygon graph. | Built via `NavMeshBuilder`. |
| `NavMeshBuilder` | Grid/test mesh generation utilities. | Used by tests and demo setup. |
| `MovementIntegrator` | Commits movement back to world store. | Reflection bridge avoids crowd compile dependency cycle. |
| `RvoSolver` | Local avoidance helper. | Agent-level steering conflict mitigation. |

### Code Examples
// Scenario: request a path and steer an agent.
```java
import org.dynamisai.navigation.*;
import org.dynamisai.core.*;

NavigationSystem nav = new DefaultNavigationSystem(NavMeshBuilder.buildGrid(16, 16, 1f, 4));
EntityId agent = EntityId.of(1);
nav.requestPath(PathRequest.of(agent, new Location(0,0,0), new Location(10,0,10))).join();
SteeringOutput out = nav.steer(agent, new Location(0,0,0), 4f);
System.out.println(out.desiredVelocity());
((DefaultNavigationSystem) nav).shutdown();
```

// Depends on sibling module: `dynamis-ai-core`
// Scenario: integrate navigation commits into world state.
```java
import org.dynamisai.navigation.*;
import org.dynamisai.core.*;

DefaultWorldStateStore world = new DefaultWorldStateStore();
NavigationSystem nav = new DefaultNavigationSystem(NavMeshBuilder.buildGrid(8, 8, 1f, 2));
MovementIntegrator integrator = new MovementIntegrator(world, nav);
integrator.tick(1L, 1f / 60f); // commits movement deltas into world snapshot queue
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `NavigationSystem` | Pathfinding and steering backend. | `dynamis-ai-test-kit` → `NavigationSystemContractTest` |

### Related Modules
- `dynamis-ai-core` — defines steering output type and world mutation sink.
- `dynamis-ai-planning` — calls navigation operators from HTN/GOAP actions.
- `dynamis-ai-crowd` — can provide local movement context to integrator bridge.

## Internals and Porting Guide

### Architecture
Navigation runs in two phases: path planning and steering. `DefaultNavigationSystem` accepts async `PathRequest` work, stores per-agent path state, and serves deterministic `steer` outputs each simulation tick. Optional hierarchical graph (`HpaGraph`) reduces long-path search work on larger meshes.

`MovementIntegrator` translates navigation output into world state changes. It intentionally uses reflection to interoperate with crowd snapshots when present, while keeping module dependencies acyclic.

### Key Design Decisions
1. Navigation exposes one stable interface (`NavigationSystem`).
Reason: planner code depends on contract, not on a concrete pathing implementation.
2. Movement integrator uses reflection for crowd coupling.
Reason: avoids navigation↔crowd compile-time cycle while preserving optional integration.

### Threading and Lifecycle
Path requests are async; steering is tick-driven and typically called from simulation thread. `DefaultNavigationSystem` owns internal executor resources and should be shut down when no longer used. `MovementIntegrator.tick` should execute once per simulation tick.

### Porting Notes
This module uses Java records, sealed interfaces, futures, and JPMS boundaries. Port path search and avoidance math directly; swap futures for native async primitives. Preserve deterministic tick stepping and avoid hidden background state mutations.

### Known Limitations and Gotchas
- Failing to call `shutdown()` on `DefaultNavigationSystem` leaves worker threads alive.
- `MovementIntegrator` reflection integration can no-op silently if expected crowd methods are absent.
- `steer()` assumes agent path state exists; request paths before steering for meaningful output.
