![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-34-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamisai-extensions

## TLDR
`dynamisai-extensions` contains minimal reference implementations for every stable DynamisAI SPI. It exists as a contributor starting point and compatibility sanity check, not a production runtime package. Without this module, new integrators start from blank implementations. With it, each SPI has a concrete baseline showing required method contracts.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamisai-extensions</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `ExampleInferenceBackend` | Stub `InferenceBackend` implementation. | Deterministic placeholder response path. |
| `ExampleVectorMemoryStore` | Stub `VectorMemoryStore` implementation. | In-memory list-backed behavior. |
| `ExampleSentenceEncoder` | Stub `SentenceEncoder` implementation. | Deterministic fixed-dimension output. |
| `ExampleTtsPipeline` | Stub `TTSPipeline` implementation. | Returns silent/placeholder render job data. |
| `ExampleNavigationSystem` | Stub `NavigationSystem` implementation. | Straight-line placeholder pathing. |
| `ExampleSaliencyFilter` | Stub `SaliencyFilter` implementation. | Pass-through percept filtering. |
| `ExampleGameEngineAdapter` | Stub adapter implementation. | Minimal lifecycle/tick wiring. |

### Code Examples
// Scenario: use extension stubs to wire a fast local integration prototype.
```java
import org.dynamisengine.ai.extensions.*;

var backend = new ExampleInferenceBackend();
var encoder = new ExampleSentenceEncoder();
var tts = new ExampleTtsPipeline();
```

### SPI Extension Points (if applicable)
No new SPI interface is defined in this module; this module demonstrates SPIs from sibling modules.

### Related Modules
- `dynamis-ai-test-kit` — extension stubs are validated against inherited contract tests.
- All core runtime modules — each example class mirrors one stable SPI from these modules.

## Internals and Porting Guide

### Architecture
Each class is intentionally minimal and directly implements one stable SPI. This keeps onboarding focused on method contracts and expected object lifecycles.

### Key Design Decisions
1. Stubs stay non-production and dependency-light.
Reason: they are reference scaffolds for extension authors.

### Threading and Lifecycle
Threading behavior follows each target SPI contract; stubs avoid extra background work.

### Porting Notes
Porting this module usually means replacing each stub with platform-specific production logic while retaining signatures and contract semantics.

### Known Limitations and Gotchas
- Stub behavior is intentionally simplistic and not suitable for production quality.
- Contract tests validate compatibility, not feature completeness or performance.
