![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-7-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-test-kit

## TLDR
`dynamis-ai-test-kit` provides reusable SPI contract tests for external implementations. It validates that custom backends and adapters conform to DynamisAI interface behavior without depending on internal classes. Without this module, integration correctness depends on ad-hoc tests per project. With it, implementers inherit a stable compatibility suite.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-test-kit</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `InferenceBackendContractTest` | Contract suite for cognition backends. | Extend and implement `createSubject()`. |
| `VectorMemoryStoreContractTest` | Contract suite for vector stores. | Validates owner filtering/removal/similarity ordering. |
| `SentenceEncoderContractTest` | Contract suite for encoders. | Checks determinism and dimensional consistency. |
| `TTSPipelineContractTest` | Contract suite for TTS pipelines. | Verifies non-null render jobs/audio presence. |
| `NavigationSystemContractTest` | Contract suite for navigation runtimes. | Checks path/steer/idempotent removal behavior. |
| `GameEngineAdapterContractTest` | Contract suite for engine adapters. | Covers lifecycle and tick consistency. |

### Code Examples
// Scenario: validate a custom vector store implementation via inherited contract tests.
```java
import org.dynamisengine.ai.testkit.VectorMemoryStoreContractTest;
import org.dynamisengine.ai.memory.VectorMemoryStore;

class MyStoreContractTest extends VectorMemoryStoreContractTest {
    @Override protected VectorMemoryStore createSubject() { return new MyStore(); }
    @Override protected int dim() { return 384; }
}
```

### SPI Extension Points (if applicable)
No SPI interfaces are defined here; this module validates SPIs from sibling modules.

### Related Modules
- `dynamis-ai-core`, `dynamis-ai-cognition`, `dynamis-ai-memory`, `dynamis-ai-navigation`, `dynamis-ai-voice`, `dynamis-ai-social`, `dynamis-ai-crowd` — all contribute interfaces covered by contract suites.

## Internals and Porting Guide

### Architecture
Each contract is an abstract JUnit class with shared assertions and abstract factory methods. Implementers subclass, provide subject creation, and run inherited test methods.

### Key Design Decisions
1. Contract tests rely on interfaces only.
Reason: third-party implementations should validate against stable public contracts, not internal defaults.

### Threading and Lifecycle
Lifecycle is test-driven; each contract class handles setup/teardown where needed.

### Porting Notes
If your ecosystem uses another test framework, translate each abstract contract into fixture-based shared test suites with required factory hooks.

### Known Limitations and Gotchas
- Some interfaces have no dedicated contract class yet.
- Timeouts in async contracts may need adjustment for slower CI environments.
