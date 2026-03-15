![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Tests](https://img.shields.io/badge/Tests-22-brightgreen) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# llm-npc

## TLDR
`llm-npc` is the runnable showcase for the full DynamisAI stack. It wires core, cognition, perception, memory, planning, navigation, voice, social, crowd, and tools into CLI and JavaFX entry points. Without this module, integration behavior is visible only through unit tests. With it, contributors can validate end-to-end runtime behavior quickly.

## Using This Module

### Maven Dependency
```xml
<dependency>
    <groupId>org.dynamisengine.ai</groupId>
    <artifactId>llm-npc</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Key Classes
| Class | Role | Notes |
|---|---|---|
| `DemoWorld` | End-to-end simulation wiring. | Selects best available backends and encoder fallbacks. |
| `DemoCli` | CLI run entrypoint. | Headless/demo tick execution. |
| `ShowcaseApplication` / `ShowcaseGui` | JavaFX showcase UI. | GUI mode for visual inspection. |
| `DemoLauncher` | Launch orchestrator. | Chooses CLI/GUI modes. |
| `ModelSetup` | Model availability checks/setup helpers. | Reports missing local assets. |

### Code Examples
// Scenario: run demo world from CLI entrypoint.
```java
import org.dynamisengine.ai.demo.DemoCli;

DemoCli.main(new String[] {"--ticks", "120"});
```

### SPI Extension Points (if applicable)
No stable SPI interfaces are defined in this module.

### Related Modules
- `dynamis-ai-tools` — provides demo reel and inspector utilities used by showcase flows.
- `dynamis-ai-voice` — voice pipeline integration is surfaced directly in demo runs.

## Internals and Porting Guide

### Architecture
Demo module is an integration composition layer, not a reusable engine API. `DemoWorld` builds subsystems, executes ticks, and exposes both CLI and JavaFX workflows for runtime validation.

### Key Design Decisions
1. Demo prefers local fallbacks when model/audio assets are absent.
Reason: contributors can run end-to-end flows on any dev machine.

### Threading and Lifecycle
CLI path runs headless sequential ticks. JavaFX path follows JavaFX application lifecycle requirements (main-thread launch).

### Porting Notes
Porting primarily means reimplementing presentation shell (CLI/GUI) around existing module APIs. Keep fallback behavior to preserve contributor onboarding experience.

### Known Limitations and Gotchas
- JavaFX launch on macOS must run on true main thread.
- Hardware audio line may be unavailable in CI or some local setups.
