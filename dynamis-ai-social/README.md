![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-126-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-social

## TLDR
`dynamis-ai-social` models how NPCs relate, coordinate, and pace behavior over time. It includes relationship/faction storage, reputation event updates, rumor propagation, schedules/needs, and drama pacing signals. Without this module, NPC interactions stay static and disconnected from emergent world events. With it, trust, rumors, routines, and pacing modifiers evolve from gameplay state.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-social</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `SocialSystem` | Top-level social runtime interface. | `DefaultSocialSystem` composes all social engines. |
| `SocialGraph` | Relationship graph store. | Holds trust/affinity/tag edges. |
| `FactionRegistry` | Faction standing store. | Maintains inter-faction standings. |
| `ReputationEngine` | Event-to-reputation delta applier. | Stateless computation over `SocialGraph`. |
| `RumorPropagator` | Rumor spread runtime. | Hop/fidelity constraints and trust-gated routing. |
| `ScheduleEngine` | NPC schedule/needs runtime. | Produces schedule-driven world-state patches. |
| `DramaManager` | Engagement-to-modifier event runtime. | Emits behavior modifiers only; no narrative writes. |
| `EngagementTracker` | Sliding-window engagement metric collector. | Fixed-size window, no unbounded growth. |
| `Relationship` | Edge payload between entities. | Trust/affinity + tags + influence. |
| `FactionStanding` | Inter-faction stance payload. | Normalized standing scalar. |
| `NeedState` / `DailySchedule` | Routine + need urgency model. | Supports interruptions and recovery. |
| `BehaviorModifierEvent` | Drama output envelope. | Consumed by higher-level gameplay tuning systems. |

### Code Examples
// Scenario: apply reputation events to update relationship trust/affinity.
```java
import org.dynamisai.social.*;
import org.dynamisai.core.EntityId;

SocialGraph graph = new SocialGraph();
ReputationEngine rep = new ReputationEngine();

ReputationEvent event = new ReputationEvent(
    EntityId.of(1), EntityId.of(2), ReputationEventType.HELPED,
    0.8f, 100L, true, EntityId.of(2)
);
rep.apply(event, graph);
```

// Scenario: seed and propagate a rumor through trusted links.
```java
import org.dynamisai.social.*;
import org.dynamisai.core.EntityId;

SocialGraph graph = new SocialGraph();
RumorPropagator propagator = new RumorPropagator(new ReputationEngine());
RumorQueue q1 = new RumorQueue();
propagator.registerQueue(EntityId.of(1), q1);

ReputationEvent e = new ReputationEvent(EntityId.of(1), EntityId.of(2), ReputationEventType.INSULTED, 0.5f, 10L, true, EntityId.of(1));
Rumor rumor = propagator.seedRumor(e);
propagator.post(EntityId.of(1), rumor);
propagator.propagate(graph, 60L);
```

// Scenario: update schedules and produce planning patch keys.
```java
import org.dynamisai.social.*;
import org.dynamisai.core.*;
import org.dynamisai.planning.WorldState;

ScheduleEngine schedules = new ScheduleEngine();
EntityId npc = EntityId.of(7);
schedules.register(npc, DailySchedule.guardPatrol(new Location(5,0,5), new Location(0,0,0)));
schedules.update(9, 600L);

WorldState patch = schedules.buildSchedulePatch(npc, new WorldState(java.util.Map.of()), 9);
System.out.println(patch.get("schedule.currentActivity"));
```

// Depends on sibling module: `dynamis-ai-core`
// Scenario: evaluate drama beat and emit behavior modifier events.
```java
import org.dynamisai.social.*;

DramaManager drama = new DramaManager(DramaManagerConfig.defaultConfig());
EngagementMetrics metrics = EngagementMetrics.neutral(120L);
var events = drama.evaluate(metrics, 120L);
System.out.println(events.size());
```

### SPI Extension Points (if applicable)
No stable SPI interface is currently exposed as extension contract in this module.

### Related Modules
- `dynamis-ai-cognition` — supplies affect/dialogue context used in social shaping and outcomes.
- `dynamis-ai-planning` — consumes squad/schedule/drama-derived tactical context.
- `dynamis-ai-core` — provides IDs/locations/ticks and shared world timeline.

## Internals and Porting Guide

### Architecture
Social runtime is a composition of focused engines around shared social state stores. `SocialGraph` and `FactionRegistry` hold durable relationship data. `ReputationEngine` maps observed events to deltas and applies clamped updates. `RumorPropagator` then distributes second-hand effects through trust-gated queues with fidelity decay.

Routine behavior is handled by `ScheduleEngine`, which tracks per-NPC activity windows and need urgency in `NpcScheduleState`. It supports interruptions and automatic expiry, then emits patch data into planning state keys rather than mutating planner internals directly.

Pacing is handled by `DramaManager` and `EngagementTracker`. Tracker accumulates sliding-window signals; manager transitions between dramatic beats and emits `BehaviorModifierEvent` outputs on transitions. This module does not write quest state, canon facts, or narrative gates.

`DefaultSocialSystem` provides the integration surface and lifecycle composition. It owns concrete engine instances and exposes them through `SocialSystem` accessors, keeping constructors backward compatible while enabling progressive subsystem additions.

### Key Design Decisions
1. Reputation and rumor are separate engines.
Reason: direct experience and second-hand propagation have different trust/fidelity semantics.
2. Drama manager emits behavior modifiers only.
Reason: pacing control must not mutate narrative authority state.
3. Schedules output planning patches, not direct planner writes.
Reason: preserves module boundaries and keeps planning ingestion explicit.
4. Relationship payload keeps trust/affinity and tags together.
Reason: numeric and categorical social signals co-evolve from the same events.

### Threading and Lifecycle
Social stores are mutable shared runtime objects; update calls should run on simulation/cognition service threads under deterministic cadence. `RumorQueue` and event-oriented helpers are thread-safe for producer/consumer patterns. `EngagementTracker` uses fixed-size windows and should be fed at tick cadence. No blocking I/O is required by core social engines.

### Porting Notes
The module uses records/enums, immutable value payloads, and explicit tick-based updates. Replace Java collection/concurrency primitives with equivalent thread-safe maps/queues in target runtime. Preserve deterministic tick-based propagation and decay logic; avoid wall-clock based drift.

### Known Limitations and Gotchas
- Rumor spread depends on relationship trust; missing graph edges can make propagation appear inactive.
- Schedule patches use string keys; planning-side key conventions must stay aligned.
- Drama beat transitions are gated by minimum tick spacing; rapid metric changes do not force immediate churn.
- Reputation deltas clamp trust/affinity to `[-1,1]`; repeated large events saturate quickly by design.
