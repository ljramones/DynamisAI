![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-64-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-perception

## TLDR
`dynamis-ai-perception` computes what NPCs can perceive instead of exposing omniscient world state directly. It turns world snapshots and sound events into filtered `PerceptionSnapshot` outputs using sensor profiles, saliency, and influence-map layers. Without this module, downstream planning and cognition reason over perfect information. With it, agents react to bounded sensory input and spatial fields that match gameplay constraints.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-perception</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `PerceptionSystem` | Top-level perception tick contract. | `DefaultPerceptionSystem` combines sensing + saliency filtering. |
| `SimulatedSenses` | Vision/hearing simulation from world state. | Uses `SensorProfileRegistry` and `SoundEventQueue`. |
| `SensorProfile` | Per-entity sensing parameters. | Includes FOV, hearing, and facing attributes. |
| `SoundEventQueue` | Tick-scoped sound input queue. | Thread-safe producer/consumer pattern. |
| `PerceptionSnapshot` | Immutable percept output per agent/tick. | Consumed by cognition/planning layers. |
| `Percept` | Individual sensory stimulus record. | Carries stimulus type, source, location, intensity. |
| `SaliencyFilter` | SPI for percept prioritization. | `DefaultSaliencyFilter` provided. |
| `InfluenceMapEngine` | Multi-layer spatial influence computation. | Produces immutable `InfluenceMapSnapshot`. |
| `InfluenceGrid` | SIMD-accelerated layer grid. | Uses `jdk.incubator.vector` operations. |

### Code Examples
// Scenario: generate a perception snapshot from simulated senses.
```java
import org.dynamisai.perception.*;
import org.dynamisai.core.*;

SensorProfileRegistry profiles = new SensorProfileRegistry();
SoundEventQueue sounds = new SoundEventQueue();
SimulatedSenses senses = new SimulatedSenses(profiles, sounds);

EntityId npc = EntityId.of(1);
profiles.register(npc, SensorProfile.defaultHuman());

WorldSnapshot snapshot = new DefaultWorldStateStore().getCurrentSnapshot();
PerceptionSnapshot ps = senses.sense(npc, snapshot);
System.out.println(ps.percepts().size());
```

// Scenario: update and sample tactical influence layers.
```java
import org.dynamisai.perception.*;
import org.dynamisai.core.*;

InfluenceMapEngine engine = new InfluenceMapEngine(64, 64, 1f, new Location(0,0,0));
engine.addCoverAt(new Location(10,0,10), 0.8f);
engine.update(new DefaultWorldStateStore().getCurrentSnapshot(), java.util.List.of());

InfluenceMapSnapshot snap = engine.snapshot(100L);
float threat = snap.sample(InfluenceLayer.THREAT, new Location(10,0,10));
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `SaliencyFilter` | Percept ranking/attention policy. | `dynamis-ai-test-kit` → `NavigationSystemContractTest` (none for saliency yet) |

### Related Modules
- `dynamis-ai-core` — provides `WorldSnapshot`, `EntityState`, and position primitives.
- `dynamis-ai-planning` — consumes influence maps and perception outputs for tactical planning.
- `dynamis-ai-cognition` — uses perception snapshots for belief updates and dialogue grounding.

## Internals and Porting Guide

### Architecture
Perception splits into two pipelines. The first pipeline produces per-agent percept lists (`SimulatedSenses` + `DefaultSaliencyFilter`) and outputs `PerceptionSnapshot`. The second pipeline updates world-scale influence layers (`InfluenceMapEngine`) used by tactical decision systems.

`InfluenceGrid` stores flat float arrays and applies bulk math (`decayAll`, `propagate`, radial influence) using the Vector API for row-wise SIMD updates. Snapshots are immutable copies so planners never observe mutable intermediate state.

### Key Design Decisions
1. Perception uses sensor profiles per entity instead of one global visibility rule.
Reason: archetypes (guards, civilians, scripted characters) need different perception envelopes.
2. Sound and visual inputs are merged only after audibility/visibility checks.
Reason: avoids conflating stimulus channels before profile-specific gating.

### Threading and Lifecycle
`SoundEventQueue` and `SensorProfileRegistry` are thread-safe for producer/consumer use. `InfluenceMapEngine` updates mutable grids, so treat update as single-writer and share immutable snapshots cross-thread. `PerceptionSnapshot` and `InfluenceMapSnapshot` are safe to read from any thread after creation.

### Porting Notes
This module uses Java records and enums heavily, Vavr only at core boundary types, and `jdk.incubator.vector` for SIMD in `InfluenceGrid`. Replace Vector API calls with platform SIMD intrinsics and keep scalar tails for remainder lanes. Keep immutable snapshot boundaries even if your runtime does not enforce records.

### Known Limitations and Gotchas
- Vision uses distance/FOV approximation; occlusion raycasts are deferred to collision integration.
- `SoundEventQueue.drainForTick` drains queued events atomically; producers must post every tick.
- Influence layers are gameplay abstractions, not physically accurate field simulation.
- SIMD path requires fallback scalar logic for portability and deterministic tails.
