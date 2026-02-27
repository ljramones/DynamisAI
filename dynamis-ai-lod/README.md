![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-20-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-lod

## TLDR
`dynamis-ai-lod` controls AI fidelity by assigning entities to runtime LOD tiers and scaling tick cadence. It prevents full-cost cognition/planning from running on every NPC every frame. Without this module, large populations overwhelm frame budgets. With it, important agents run at high frequency while distant/background agents run at reduced rates.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-lod</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `AILODPolicy` | Tier assignment and cached evaluation coordinator. | Implements core `org.dynamisai.core.AILODPolicy`. |
| `ImportanceEvaluator` | SPI for per-entity importance scoring. | Default evaluator provided. |
| `DefaultImportanceEvaluator` | Distance/threat/dialogue-based scorer. | Outputs `ImportanceScore` + mapped `LodTier`. |
| `TickScaler` | Tick-rate scaling helper by `LodTier`. | Also answers per-tick `shouldTick`. |
| `ImportanceScore` | Scoring result record. | Includes tier and tick metadata. |

### Code Examples
// Scenario: evaluate tiers and check whether an entity should tick.
```java
import org.dynamisai.lod.*;
import org.dynamisai.core.*;

AILODPolicy policy = new AILODPolicy(new DefaultImportanceEvaluator());
WorldSnapshot snapshot = new DefaultWorldStateStore().getCurrentSnapshot();
policy.evaluate(snapshot);

boolean run = policy.shouldRunAi(EntityId.of(1), snapshot.tick(), snapshot);
System.out.println(run);
```

// Scenario: scale base rate for each LOD tier.
```java
import org.dynamisai.lod.TickScaler;
import org.dynamisai.core.LodTier;

double baseHz = 60.0;
System.out.println(TickScaler.scaleRate(baseHz, LodTier.TIER_0));
System.out.println(TickScaler.scaleRate(baseHz, LodTier.TIER_3));
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `ImportanceEvaluator` | Importance scoring policy for tier assignment. | No dedicated contract class yet |

### Related Modules
- `dynamis-ai-core` — supplies `LodTier`, `WorldSnapshot`, and budget-governor integration point.
- `dynamis-ai-crowd` — can consume synchronized tier assignments for crowd-level fidelity.

## Internals and Porting Guide

### Architecture
LOD evaluation is batched and cached in `AILODPolicy`. Every evaluation window, the policy computes `ImportanceScore` values via `ImportanceEvaluator`, maps them to `LodTier`, and stores current assignments. Between windows, tick decisions use cached tiers.

`TickScaler` centralizes tier-to-rate mapping so all consumers apply identical cadence rules.

### Key Design Decisions
1. `LodTier` is defined in core, not lod module.
Reason: crowd/core/lod share one enum without cyclic dependencies.
2. Evaluation cache interval is explicit.
Reason: avoids tier thrash from per-frame noise.

### Threading and Lifecycle
`AILODPolicy` holds mutable tier cache and should be updated on simulation thread cadence. Read operations are lightweight and deterministic for same snapshot input.

### Porting Notes
Porting requires immutable world snapshot input and deterministic tick arithmetic. Replace Java maps with target equivalents and preserve modulo-based should-tick logic per tier.

### Known Limitations and Gotchas
- Policy quality depends on available world-state signals; missing observer/threat data flattens scores.
- Cached evaluation interval trades responsiveness for stability.
- `currentTier` returns background tier for unknown entities by design.
