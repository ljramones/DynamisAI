# DynamisAI API Guide

**Version:** 1.0.0-SNAPSHOT  
**JDK:** 25 (preview + Vector API incubator)  
**Build system:** Maven 3.9+

---

## Overview

DynamisAI is a modular NPC AI engine. Modules are JPMS-separated and depend upward without circular imports.

- `dynamis-ai-core`
- `dynamis-ai-perception`
- `dynamis-ai-cognition`
- `dynamis-ai-memory`
- `dynamis-ai-planning`
- `dynamis-ai-navigation`
- `dynamis-ai-voice`
- `dynamis-ai-social`
- `dynamis-ai-crowd`
- `dynamis-ai-tools`
- `dynamis-ai-demo` (runnable demo app)

---

## Quick Start (Core + Perception)

```xml
<dependency>
  <groupId>org.dynamisai</groupId>
  <artifactId>dynamis-ai-core</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.dynamisai</groupId>
  <artifactId>dynamis-ai-perception</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.dynamisai</groupId>
  <artifactId>dynamis-ai-cognition</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```java
DefaultWorldStateStore world = new DefaultWorldStateStore();
EntityId npc = EntityId.of(1L);

world.enqueueChange(new WorldChange.EntityStateChange(
    npc, new EntityState(npc, new Location(0, 0, 0), Map.of())));
world.commitTick();

DefaultBudgetGovernor governor = new DefaultBudgetGovernor(16);
DefaultPerceptionSystem perception = new DefaultPerceptionSystem();

governor.register(new AITaskNode(
    "perception", 2, Priority.HIGH, DegradeMode.CACHED,
    () -> perception.tick(npc, AffectVector.neutral(), world),
    () -> {}
));

governor.runFrame(world.getCurrentTick(), world.getCurrentSnapshot());
```

---

## Module Reference

### `dynamis-ai-core`

Key types:
- `EntityId`, `Location`, `ThreatLevel`
- `EntityState`, `WorldSnapshot`, `WorldStateStore`
- `DefaultBudgetGovernor`, `AITaskNode`, `FrameBudgetReport`

Patterns:
- Queue world updates via `enqueueChange(...)`
- Publish immutable snapshots via `commitTick()`
- Schedule AI work via `DefaultBudgetGovernor`

### `dynamis-ai-perception`

Key types:
- `DefaultPerceptionSystem`, `PerceptionSnapshot`, `Percept`, `StimulusType`

Usage:
```java
PerceptionSnapshot snap = perception.tick(npc, AffectVector.neutral(), worldStore);
snap.mostSalientPercept().ifPresent(p -> System.out.println(p.salienceScore()));
```

### `dynamis-ai-cognition`

Key types:
- `CognitionService`, `DefaultCognitionService`
- `DialogueRequest`, `DialogueResponse`, `AffectVector`
- `InferenceBackend` implementations (`MockInferenceBackend`, etc.)

Usage:
```java
DefaultCognitionService cognition = new DefaultCognitionService(new MockInferenceBackend());
DialogueRequest req = new DialogueRequest(
    npc, target, "Who goes there?",
    new WorldFacts(Map.of(), List.of(), ThreatLevel.NONE, new Location(0, 0, 0), null),
    AffectVector.neutral(),
    worldStore.getCurrentSnapshot());

DialogueResponse resp = cognition.requestDialogue(req).get(500, TimeUnit.MILLISECONDS);
```

### `dynamis-ai-memory`

Key types:
- `DefaultMemoryLifecycleManager`, `MemoryRecord`, `MemoryStats`
- `VectorMemoryStore`, `OffHeapVectorMemoryStore`, `EmbeddingVector`, `SimilarityResult`

Usage:
```java
DefaultMemoryLifecycleManager memory =
    new DefaultMemoryLifecycleManager(MemoryBudget.tier1(), new InHeapVectorMemoryStore());

memory.addRawEvent(MemoryRecord.rawEvent(npc, "Player attacked", "payload", 0.9f));
memory.consolidate(npc);
MemoryStats stats = memory.getStats(npc);
```

### `dynamis-ai-planning`

Key types:
- `DefaultHtnPlanner`, `HtnTask`, `TaskLibrary`, `Plan`, `WorldState`
- `NavigationOperators` for nav-wired operators

Usage:
```java
WorldState ws = WorldState.withoutNav(
    npc, tick, AffectVector.neutral(), ThreatLevel.NONE,
    perceptionSnapshot, memoryStats, Map.of("nearPlayer", true));

DefaultHtnPlanner planner = new DefaultHtnPlanner();
Plan plan = planner.plan(
    TaskLibrary.surviveTask(new AtomicInteger(), new AtomicInteger(), new AtomicInteger()),
    ws,
    PlanningBudget.standard());
```

### `dynamis-ai-navigation`

Key types:
- `NavMesh`, `NavMeshBuilder`, `NavigationSystem`, `DefaultNavigationSystem`
- `PathRequest`, `PathResult`, `SteeringOutput`, `RvoAgent`

Usage:
```java
DefaultNavigationSystem nav = new DefaultNavigationSystem(
    NavMeshBuilder.buildGrid(16, 16, 2f, 4));

nav.requestPath(PathRequest.of(npc, fromLoc, toLoc));
SteeringOutput steer = nav.steer(npc, currentLoc, 4f);
```

### `dynamis-ai-social`

Key types:
- `DefaultSocialSystem`, `SocialContext`, `SocialInfluence`, `SocialDialogueShaper`
- `Relationship`, `RelationshipTag`, `FactionRegistry`, `DialogueHistory`

Usage:
```java
DefaultSocialSystem social = new DefaultSocialSystem();
social.adjustTrust(guard, player, -0.4f);
social.tagRelationshipBoth(guard, player, RelationshipTag.ENEMY);

DialogueRequest shaped = SocialDialogueShaper.shape(request, social);
```

### `dynamis-ai-crowd`

Key types:
- `DefaultCrowdSystem`, `CrowdSnapshot`, `CrowdLod`
- `FormationType`, `Formation`, `GroupId`, `CrowdAgent`

Usage:
```java
DefaultCrowdSystem crowd = new DefaultCrowdSystem();
GroupId g = crowd.createGroup(FormationType.LINE);
crowd.addToGroup(g, guard1, new Location(2, 0, 2));
crowd.addToGroup(g, guard2, new Location(4, 0, 2));
crowd.setGroupGoal(g, new Location(14, 0, 2));
CrowdSnapshot snap = crowd.tick(tick, 0.016f);
```

### `dynamis-ai-voice`

Key types:
- `TTSPipeline`, `MockTTSPipeline`, `VoiceRenderJob`
- `AnimisBridge`, `DefaultAnimisBridge`, `PhysicalVoiceContext`

Usage:
```java
MockTTSPipeline tts = new MockTTSPipeline();
DefaultAnimisBridge animis = new DefaultAnimisBridge();

tts.render(dialogueResponse, PhysicalVoiceContext.calm(), npc)
   .thenAccept(animis::submitVoiceJob);
```

### `dynamis-ai-tools`

Integration helpers and demo harnesses for runtime wiring across modules, including DynamisAudio bridge integration.

### `dynamis-ai-demo`

Interactive CLI scenario that wires all modules in one runtime loop.

---

## Integration Patterns

1. Minimal NPC: `core + perception + cognition`
2. Patrol NPC: add `planning + navigation + crowd`
3. Social NPC: add `memory + social`
4. Full stack: all modules (see `dynamis-ai-demo`)

---

## Architecture Constraints

- Keep similarity search async (`findSimilar()`)
- Keep world snapshots immutable
- Keep frame work under budget via `BudgetGovernor`
- Avoid circular JPMS dependencies
- Keep crowd reads lock-free through immutable `CrowdSnapshot`
- Keep navigation/path requests non-blocking

---

## Running the Demo

```bash
# Install all library modules first
for dir in dynamis-ai-core dynamis-ai-cognition dynamis-ai-perception \
           dynamis-ai-memory dynamis-ai-planning dynamis-ai-navigation \
           dynamis-ai-voice dynamis-ai-social dynamis-ai-crowd dynamis-ai-tools; do
  cd $dir && mvn clean install -DskipTests && cd ..
done

# Run demo
cd dynamis-ai-demo && mvn exec:java
```

Controls:
- `A` approach
- `H` hostile act
- `F` flee
- `W` wait
- `S` speak

Writes `demo-report.json` on exit.

---

## Running Tests

```bash
for dir in dynamis-ai-core dynamis-ai-cognition dynamis-ai-perception \
           dynamis-ai-memory dynamis-ai-planning dynamis-ai-navigation \
           dynamis-ai-voice dynamis-ai-social dynamis-ai-crowd dynamis-ai-tools \
           dynamis-ai-demo; do
  cd $dir && mvn clean test && cd ..
done
```
