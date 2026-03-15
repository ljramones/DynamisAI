![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-67-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-cognition

## TLDR
`dynamis-ai-cognition` turns world/perception context into dialogue decisions and affect outputs. It defines the inference abstraction (`InferenceBackend`) and the runtime orchestration service (`CognitionService`) used by higher-level gameplay systems. Without this module, NPC communication and intent handling become tightly coupled to one model provider. With it, you can swap local or remote inference backends while preserving deterministic request flow and fallback behavior.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-cognition</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `CognitionService` | High-level dialogue inference service. | Async API with fallback path and queue depth reporting. |
| `DefaultCognitionService` | Default service implementation. | Manages cache, backends, belief registry, deterministic requests. |
| `InferenceBackend` | SPI for model providers. | Implement for Jlama/Ollama/custom backends. |
| `JlamaInferenceBackend` | In-process local model backend. | Uses `jlama.core`; best for pure-JVM local deployment. |
| `OllamaInferenceBackend` | HTTP backend for Ollama runtime. | Network boundary and timeout behavior apply. |
| `MockInferenceBackend` | Deterministic test backend. | Useful for tests and offline/no-model runs. |
| `DialogueRequest` | Full input envelope for inference. | Includes speaker/target/world facts/affect. |
| `DialogueResponse` | Inference output envelope. | Text + affect vector + behavior hint fields. |
| `InferenceRequest` | Seed-aware inference wrapper. | Supports deterministic seeded invocation. |
| `BeliefModel` | Per-entity first/second-order belief store. | Supports decay and reinforcement. |
| `BeliefModelRegistry` | Registry for entity belief models. | Creates-on-demand and decays all models. |
| `AffectVector` | Core affect state vector. | Used across cognition/voice/social. |

### Code Examples
// Scenario: build cognition service with mock backend and request a line.
```java
import org.dynamisengine.ai.cognition.*;
import org.dynamisengine.ai.core.*;

CognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
DialogueRequest request = new DialogueRequest(
    EntityId.of(1),
    EntityId.of(2),
    "What are you doing here?",
    new WorldFacts(java.util.Map.of(), java.util.List.of(), ThreatLevel.NONE, new Location(0,0,0), null),
    AffectVector.neutral(),
    null
);
DialogueResponse response = cognition.requestDialogue(request).join();
System.out.println(response.text());
```

// Scenario: deterministic inference calls using an explicit seed.
```java
import org.dynamisengine.ai.cognition.*;

CognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
DialogueRequest req = /* build request */ null;
DialogueResponse a = cognition.inferDeterministic(req, 42L).join();
DialogueResponse b = cognition.inferDeterministic(req, 42L).join();
```

// Scenario: update and query belief state for one NPC.
```java
import org.dynamisengine.ai.cognition.*;
import org.dynamisengine.ai.core.EntityId;

BeliefModel model = new BeliefModel(EntityId.of(7), BeliefDecayPolicy.defaultPolicy());
model.assertBelief("entity.player.visible", true, 0.9f, 100L);
model.assertSecondOrder(EntityId.of(1), "entity.player.visible", 0.6f, 100L);
model.decay(160L);
System.out.println(model.getBelief("entity.player.visible").orElseThrow().confidence());
```

// Depends on sibling module: `dynamis-ai-core`
// Scenario: ground a request with core world facts and threat level.
```java
import org.dynamisengine.ai.cognition.*;
import org.dynamisengine.ai.core.*;

WorldFacts facts = new WorldFacts(
    java.util.Map.of("weather", "fog"),
    java.util.List.of(EntityId.of(99)),
    ThreatLevel.HIGH,
    new Location(3,0,4),
    null
);
DialogueRequest req = new DialogueRequest(EntityId.of(5), EntityId.of(99), "Report status.", facts, AffectVector.neutral(), null);
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `InferenceBackend` | LLM runtime/provider integration. | `dynamis-ai-test-kit` → `InferenceBackendContractTest` |

### Related Modules
- `dynamis-ai-core` — provides `EntityId`, `WorldFacts`, and deterministic tick context for requests.
- `dynamis-ai-voice` — consumes dialogue text and affect outputs to render speech and visemes.
- `dynamis-ai-tools` — inspects beliefs and decision traces through debugger/inspector tooling.

## Internals and Porting Guide

### Architecture
The cognition module centers on `DefaultCognitionService`. It receives `DialogueRequest`, routes it through an `InferenceBackend`, normalizes/parses output, applies fallback if inference fails, and returns `DialogueResponse`. Caching, deterministic request wrapping, and queue metrics are handled at this service layer so backends remain focused on transport/runtime concerns.

Belief handling is local and explicit. `BeliefModel` stores first-order and second-order beliefs per entity, while `BeliefModelRegistry` owns lifecycle and periodic decay. This keeps inference orchestration separate from long-lived epistemic state while still allowing request grounding through `BeliefContextBuilder`.

Backend abstraction is strict: `InferenceBackend` accepts `InferenceRequest` so deterministic seed metadata can flow end-to-end, regardless of whether a concrete backend currently supports sampler seeding. Mock and offline paths keep runtime behavior available in CI and on machines without model assets.

### Key Design Decisions
1. `InferenceBackend` is a stable interface.
Reason: model runtimes and deployment targets change faster than gameplay contracts.
2. `InferenceRequest` wraps `DialogueRequest`.
Reason: deterministic seed metadata is orthogonal to dialogue payload shape.
3. Belief decay is policy-driven (`BeliefDecayPolicy`).
Reason: different NPC classes need different memory persistence behavior.

### Threading and Lifecycle
`CognitionService` methods return `CompletableFuture` and are safe for asynchronous integration from simulation code. `DefaultCognitionService` maintains internal mutable state (cache + registry), so treat each service instance as a shared singleton per simulation context rather than per-call construction. Backends own their transport/model lifecycle; call `CognitionService.shutdown()` on teardown.

### Porting Notes
This module uses Java records, sealed-style data modeling, and `CompletableFuture` async composition. Replace futures with language-native async primitives. If your target lacks records, use immutable data classes. If you remove JPMS, preserve public/internal package boundaries so backend internals stay separate from stable API contracts.

### Known Limitations and Gotchas
- Not every backend can enforce deterministic seeding; seed fields still propagate for contract consistency.
- `MockInferenceBackend` is deterministic by construction and does not represent real latency or model variance.
- Belief reinforcement keys are convention-based strings; cross-module producers must keep key naming consistent.
- Cache behavior depends on exact request shape; minor prompt changes can bypass warm entries.
