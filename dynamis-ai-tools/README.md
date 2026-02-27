![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-91-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-tools

## TLDR
`dynamis-ai-tools` provides runtime diagnostics and offline validation utilities around the core AI stack. It includes affective/debug snapshots, timeline replay inspector, and headless simulation baking assertions. Without this module, regression detection and deep AI introspection require ad-hoc tooling per project. With it, teams can inspect decision state and run deterministic offline bake checks before QA cycles.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-tools</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `AffectiveStateDebugger` | Captures per-NPC debug snapshots. | Read-only capture path; no inference triggers. |
| `NpcDebugSnapshot` | Full NPC debug payload at one tick. | Includes affect radar, decision trace, beliefs, percept overlay. |
| `DebugSnapshotHistory` | Bounded per-entity snapshot history. | Fixed-size deques per NPC. |
| `AIInspector` | Snapshot/replay inspector coordinator. | Opens replay sessions and exports timeline data. |
| `SnapshotStore` | Bounded world snapshot record store. | JSON-lines export/import for offline analysis. |
| `ReplaySession` | Time-travel cursor over stored ticks. | Seek/step semantics with debug snapshot lookup. |
| `SimulationBaker` | Headless offline simulation runner. | Executes scenarios with assertion checks. |
| `BakeAssertion` | Stable bake assertion SPI. | Implement custom frame-level validation rules. |
| `CommonBakeAssertions` | Built-in assertion library. | Speed, dialogue quality, stall detection checks. |

### Code Examples
// Scenario: capture and format one NPC debug snapshot.
```java
import org.dynamisai.tools.*;
import org.dynamisai.cognition.*;
import org.dynamisai.core.*;
import org.dynamisai.perception.*;

CognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
AffectiveStateDebugger debugger = new AffectiveStateDebugger(cognition);
NpcDebugSnapshot snap = debugger.capture(
    EntityId.of(1),
    new DefaultWorldStateStore().getCurrentSnapshot(),
    PerceptionSnapshot.of(EntityId.of(1), java.util.List.of(), 1L),
    null,
    AffectVector.neutral(),
    0.4f,
    1L
);
System.out.println(AffectiveStateDebugger.format(snap));
```

// Scenario: run a headless bake with built-in assertions.
```java
import org.dynamisai.tools.*;
import org.dynamisai.core.*;

SimulationBaker baker = new SimulationBaker(
    DynamisAiEngine.builder().build(),
    new HeadlessGameEngineAdapter()
);
BakeReport report = baker.bake(BakeScenario.quick("ci-smoke", 100));
System.out.println(report.summary());
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `BakeAssertion` | Offline simulation validation rules. | No dedicated contract class yet |

### Related Modules
- `dynamis-ai-core` — provides engine tick integration and output frames inspected by tools.
- `dynamis-ai-cognition` — supplies beliefs/affect consumed by debugger snapshots.
- `dynamis-ai-perception` — contributes percept overlays in debug captures.

## Internals and Porting Guide

### Architecture
Tools split into two tracks: online observability and offline validation. Observability track (`AffectiveStateDebugger`, `AIInspector`, replay classes) records immutable debug/snapshot payloads and supports post-hoc timeline traversal. Validation track (`SimulationBaker`, `BakeAssertion`) drives headless ticks at maximum speed and reports assertion failures.

`AIInspector` and `SnapshotStore` stay bounded to prevent unbounded memory growth. Export/import uses JSON lines to keep artifacts stream-friendly for CI ingestion.

### Key Design Decisions
1. Core inspector attachment is reflective.
Reason: avoids core→tools dependency cycles while keeping optional instrumentation.
2. Bake assertions are interface-based.
Reason: projects can add domain checks without modifying tools internals.

### Threading and Lifecycle
Tool record types are immutable. Histories/stores are mutable and thread-safe for append/read use; replay sessions maintain local cursor state. `SimulationBaker` runs synchronously and blocks until scenario completion.

### Porting Notes
This module mainly uses immutable records, bounded collections, and plain-string serialization. Replace Java deques/maps with equivalent bounded structures. Preserve deterministic tick order in bakes and replay seek semantics.

### Known Limitations and Gotchas
- JSON-line import intentionally skips malformed lines silently.
- `SimulationBaker` assumes headless adapter ownership and initializes adapter in constructor.
- Replay sessions are read-only views over recorded data; they do not re-simulate world state.
- Debug capture completeness depends on caller providing plan/perception/affect inputs.
