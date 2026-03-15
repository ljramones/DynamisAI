![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-82-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-voice

## TLDR
`dynamis-ai-voice` renders dialogue into playable audio, visemes, and blendshape animation data. It separates synthesis (`TTSPipeline`) from runtime dispatch (`AnimisBridge`) and supports rule-based and waveform-driven viseme extraction. Without this module, text output from cognition cannot become embodied character performance. With it, voice playback and facial animation data travel through a stable, engine-agnostic contract.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>dynamis-ai-voice</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `TTSPipeline` | Stable synthesis SPI. | Async render contract returning `VoiceRenderJob`. |
| `DjlTtsPipeline` | Main DJL-backed pipeline. | Produces `SynthesisResult` (audio + visemes + blendshapes). |
| `VisemeExtractor` | Stable viseme extraction SPI. | Rule-based and waveform implementations available. |
| `RuleBasedVisemeExtractor` | Transcript-driven viseme extractor. | Deterministic, no model assets required. |
| `WaveformVisemeExtractor` | Audio-model extractor with fallback. | Falls back to rule-based when model is absent. |
| `BlendshapeMapper` | Maps viseme+affect to blendshape frames. | Uses `BlendshapeTable` mappings and clamping. |
| `AnimisBridge` | Runtime dispatch contract to animation/audio system. | `DefaultAnimisBridge` queues jobs and intent signals. |
| `VoiceRenderJob` | Render output payload. | Contains audio stream, visemes, metadata. |
| `SynthesisResult` | Intermediate synthesis payload. | Carries `AudioStream`, visemes, blendshape frames. |
| `AffectToVoiceStyle` | Affect-to-prosody mapping helper. | Maps affect vector to style controls. |

### Code Examples
// Scenario: synthesize dialogue with the default DJL pipeline.
```java
import org.dynamisengine.ai.voice.*;
import org.dynamisengine.ai.cognition.*;
import org.dynamisengine.ai.core.EntityId;

TTSPipeline tts = new DjlTtsPipeline();
DialogueResponse response = DialogueResponse.of("Hold your ground.", AffectVector.neutral());
VoiceRenderJob job = tts.render(response, PhysicalVoiceContext.calm(), EntityId.of(1)).join();
System.out.println(job.primaryAudio().sampleRate());
```

// Scenario: extract visemes from synthesized audio using fallback-safe extractor.
```java
import org.dynamisengine.ai.voice.*;

VisemeExtractor extractor = new WaveformVisemeExtractor();
AudioBuffer audio = new AudioBuffer(new float[22050], 22050, 1);
var visemes = extractor.extract(audio, "Move to cover now");
System.out.println(visemes.size());
```

// Scenario: map visemes and affect to blendshape frames.
```java
import org.dynamisengine.ai.voice.*;
import org.dynamisengine.ai.cognition.AffectVector;

BlendshapeMapper mapper = new BlendshapeMapper(BlendshapeTable.defaultHumanoid());
var frames = mapper.map(
    java.util.List.of(new VisemeTimestamp("AI", 0f, 0.1f, 0.85f)),
    AffectVector.neutral()
);
System.out.println(frames.getFirst().weights().get("jawOpen"));
```

// Depends on sibling module: `dynamis-ai-core`
// Scenario: submit rendered voice into bridge for runtime dispatch.
```java
import org.dynamisengine.ai.voice.*;
import org.dynamisengine.ai.core.EntityId;

AnimisBridge bridge = new DefaultAnimisBridge();
VoiceRenderJob job = /* produced by TTSPipeline */ null;
bridge.submitVoiceJob(job, EntityId.of(1));
```

### SPI Extension Points (if applicable)
| Interface | What you replace | Contract test location |
|---|---|---|
| `TTSPipeline` | TTS engine backend. | `dynamis-ai-test-kit` → `TTSPipelineContractTest` |
| `VisemeExtractor` | Phoneme/viseme extraction backend. | No dedicated contract class yet |
| `AnimisBridge` | Runtime audio/animation dispatch layer. | No dedicated contract class yet |

### Related Modules
- `dynamis-ai-cognition` — provides dialogue text and affect vectors for synthesis.
- `dynamis-ai-tools` — bridges voice jobs into DynamisAudio in demo/tooling runtime.
- `dynamis-ai-demo` — exercises full pipeline in CLI/GUI showcase.

## Internals and Porting Guide

### Architecture
Voice runtime has three stages: synthesis, articulation extraction, and dispatch. `TTSPipeline` implementations generate PCM and intermediate metadata (`SynthesisResult`). Viseme extractors convert transcript/audio into `VisemeTimestamp` sequences. Blendshape mapping then combines visemes with affect into rig-facing frame data.

`DjlTtsPipeline` coordinates these stages and returns `VoiceRenderJob` objects consumed by `AnimisBridge`. This keeps rendering concerns separate from playback scheduling and engine-specific output devices.

Fallback behavior is explicit. `WaveformVisemeExtractor` can load ONNX-backed extraction but delegates to `RuleBasedVisemeExtractor` when assets are absent. This guarantees deterministic runtime behavior in CI/offline environments.

### Key Design Decisions
1. `VisemeTimestamp.weight` represents blend weight, not model confidence.
Reason: downstream animation systems consume articulation magnitude directly.
2. `AudioBuffer` lives in voice module and carries raw PCM.
Reason: viseme extraction runs before any external audio bridge conversion.
3. Bridge and pipeline are separate interfaces.
Reason: synthesis backend portability and runtime dispatch portability are independent concerns.

### Threading and Lifecycle
`TTSPipeline.render` is async and typically called from cognition/simulation services. `AnimisBridge` methods accept pre-rendered payloads and should be processed on audio/animation runtime threads per integration needs. Close/shutdown semantics depend on concrete engines (`DjlTtsPipeline`, ONNX sessions, hardware bridges).

### Porting Notes
This module uses Java records, DJL APIs, and ONNX-backed model wrappers. Porters should substitute runtime-specific inference and audio APIs while keeping contracts stable (`TTSPipeline`, `VisemeExtractor`, `AnimisBridge`). Preserve immutable payload boundaries (`SynthesisResult`, `VoiceRenderJob`) to prevent racey cross-thread mutation.

### Known Limitations and Gotchas
- DJL predictor/session objects have thread-safety constraints; avoid shared mutable predictor instances.
- Waveform extractor quality depends on model availability; fallback path is deterministic but lower fidelity.
- Blendshape table names must match target rig naming conventions.
- Bridge implementations must handle missing audio hardware gracefully in headless/demo contexts.
