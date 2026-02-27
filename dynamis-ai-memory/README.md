![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-59-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-memory

## TLDR
`dynamis-ai-memory` manages long-lived NPC memory retrieval with vector similarity search and lifecycle controls. It provides a high-performance off-heap vector store, in-heap convenience wrapper, and sentence encoding SPI for semantic lookup. Without this module, cognition and planning cannot query prior experiences efficiently. With it, memory retrieval stays fast, bounded, and replaceable via stable interfaces.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisai</groupId>
    <artifactId>dynamis-ai-memory</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `VectorMemoryStore` | Stable memory retrieval SPI. | Async similarity query contract. |
| `OffHeapVectorMemoryStore` | Panama-backed SIMD vector index. | Manual `close()` required for off-heap segment lifecycle. |
| `InHeapVectorMemoryStore` | Convenience wrapper over off-heap store. | Supports string-based store/query helpers via encoder. |
| `SentenceEncoder` | Stable embedding SPI. | Implement for custom embedding models. |
| `MockSentenceEncoder` | Deterministic hash encoder. | Default fallback for tests/offline behavior. |
| `MiniLmSentenceEncoder` | ONNX MiniLM encoder integration. | Falls back to mock encoder if model files are absent. |
| `MemoryLifecycleManager` | Lifecycle contract for promote/archive/prune passes. | `DefaultMemoryLifecycleManager` provided. |
| `MemoryRecord` | Memory item payload. | Owner, summary, payload, salience, stage. |
| `SimilarityResult` | Query hit + similarity score. | Returned by vector queries. |
| `EmbeddingVector` | Dense float vector wrapper. | Normalization and validation helpers. |

### Code Examples
// Scenario: store and retrieve a memory vector hit.
```java
import org.dynamisai.memory.*;
import org.dynamisai.core.EntityId;

VectorMemoryStore store = new OffHeapVectorMemoryStore(384, 64);
MemoryRecord rec = MemoryRecord.create(EntityId.of(1), "player entered tavern", "payload", 0.6f);
float[] raw = new float[384]; raw[0] = 1f;
store.store(rec, new EmbeddingVector(raw));

var hits = store.findSimilar(new EmbeddingVector(raw), EntityId.of(1), 5).join();
System.out.println(hits.getFirst().record().summary());
store.close();
```

// Scenario: use encoder-backed in-heap convenience APIs.
```java
import org.dynamisai.memory.*;
import org.dynamisai.core.EntityId;

InHeapVectorMemoryStore store = new InHeapVectorMemoryStore(new MockSentenceEncoder());
store.store(MemoryRecord.create(EntityId.of(1), "guard saw player", "...", 0.5f));
var results = store.findSimilar(EntityId.of(1), "player", 3);
System.out.println(results.size());
store.close();
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `VectorMemoryStore` | Vector DB/index backend. | `dynamis-ai-test-kit` → `VectorMemoryStoreContractTest` |
| `SentenceEncoder` | Embedding model pipeline. | `dynamis-ai-test-kit` → `SentenceEncoderContractTest` |

### Related Modules
- `dynamis-ai-cognition` — reads/writes episodic memory context for dialogue and decision grounding.
- `dynamis-ai-tools` — inspects memory-driven traces in debugger and bake workflows.
- `dynamis-ai-demo` — uses in-heap store with auto-selected sentence encoder fallback.

## Internals and Porting Guide

### Architecture
Memory storage uses a layered design: `InHeapVectorMemoryStore` exposes ergonomic APIs and delegates to `OffHeapVectorMemoryStore` for indexing/search. Retrieval is asynchronous, owner-scoped, and score-sorted. Lifecycle policies (`DefaultMemoryLifecycleManager`) run promotion/archive/prune passes independent of index mechanics.

Off-heap storage packs vectors into `MemorySegment` and uses SIMD dot products for candidate scoring. Metadata maps maintain ID/slot bookkeeping, while vector lanes operate over contiguous float memory for throughput.

### Key Design Decisions
1. Off-heap storage is the primary implementation.
Reason: heap pressure from large vector pools is avoided and memory layout stays cache-friendly.
2. `InHeapVectorMemoryStore()` defaults to mock encoder.
Reason: backward compatibility and deterministic tests without external model assets.

### Threading and Lifecycle
Vector stores are mutable shared objects. `OffHeapVectorMemoryStore` uses internal locking for concurrent read/write safety, but `close()` must run once at shutdown. Query APIs return futures for asynchronous integration. Encoders must be thread-safe per `SentenceEncoder` contract.

### Porting Notes
This module uses Project Panama foreign memory APIs and Vector API SIMD intrinsics in hot paths. Porters need explicit off-heap allocators and SIMD primitives (AVX2/NEON equivalents). Records/interfaces map cleanly to data classes and traits. Keep explicit close semantics for native memory ownership.

### Known Limitations and Gotchas
- `OffHeapVectorMemoryStore` must be closed; GC does not own native segment lifecycle.
- Encoder dimensionality must match store dimensionality.
- `MiniLmSentenceEncoder` falls back when ONNX files are missing; check `isLive()` if semantic quality is required.
- Query futures can time out in callers; choose timeouts per gameplay budget.
