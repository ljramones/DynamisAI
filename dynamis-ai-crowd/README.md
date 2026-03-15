![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-32-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-crowd

## TLDR
`dynamis-ai-crowd` manages group-level agent organization: formations, slots, group snapshots, and crowd LOD controls. It fills the gap between single-agent navigation and large-scene crowd behavior. Without this module, coordinated squads and ambient crowd groups require custom glue in each game integration. With it, group geometry and membership updates are centralized.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-crowd</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `CrowdSystem` | Group/crowd runtime interface. | Implemented by `DefaultCrowdSystem`. |
| `DefaultCrowdSystem` | Main crowd runtime. | Creates groups, tracks agents, produces snapshots. |
| `CrowdGroup` | Group-level membership + formation state. | Uses `Formation` slots. |
| `Formation` | Computes slot offsets by `FormationType`. | Deterministic geometry helpers. |
| `LodController` | Crowd LOD assignment controller. | Can sync from core LOD policy tiers. |
| `CrowdSnapshot` | Immutable crowd-at-tick view. | For tools/planners/observers. |

### Code Examples
// Scenario: create a group and tick crowd simulation.
```java
import org.dynamisengine.ai.crowd.*;
import org.dynamisengine.ai.core.*;

DefaultCrowdSystem crowd = new DefaultCrowdSystem();
GroupId group = crowd.createGroup(FormationType.LINE);
crowd.addToGroup(group, EntityId.of(1), new Location(0,0,0));
crowd.addToGroup(group, EntityId.of(2), new Location(1,0,0));
crowd.tick(1L, 1f / 60f);
System.out.println(crowd.latestSnapshot().totalAgents());
```

### SPI Extension Points (if applicable)
No stable extension interface is currently exposed from this module.

### Related Modules
- `dynamis-ai-navigation` — consumes crowd context for local movement integration.
- `dynamis-ai-core` — provides IDs, locations, and shared LOD tier enum.

## Internals and Porting Guide

### Architecture
Crowd runtime groups agents under `GroupId`, assigns formation slots, and publishes immutable `CrowdSnapshot` output each tick. `LodController` controls fidelity tiers per agent/group and can synchronize with external AI LOD policies.

### Key Design Decisions
1. Formation math is isolated in `Formation`/`FormationSlot`.
Reason: group geometry stays independent from navigation/pathfinding internals.

### Threading and Lifecycle
`DefaultCrowdSystem` is mutable and should tick from simulation thread. Snapshot reads are safe post-tick.

### Porting Notes
This module primarily uses records/enums and deterministic geometry math. Port directly with immutable snapshot outputs and explicit group registries.

### Known Limitations and Gotchas
- Group membership/state is runtime-managed; remove agents explicitly when despawned.
- LOD policy sync is optional; if unused, local crowd LOD rules remain active.
