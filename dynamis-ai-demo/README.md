![Java](https://img.shields.io/badge/Java-25-blue) ![License](https://img.shields.io/badge/License-Apache%202.0-green) ![Build](https://img.shields.io/badge/Build-Maven-orange)

# dynamis-ai-demo

## TLDR
`dynamis-ai-demo` is now a multi-module parent for independent demo applications. Each child module under this directory is a standalone demo with its own dependencies, entrypoints, and tests.

## Modules
- [`llm-npc`](./llm-npc/README.md) — the original end-to-end NPC demo (moved from the previous single-module `dynamis-ai-demo`).

## Build
From repo root:
```bash
mvn -pl dynamis-ai-demo -am test
```

From this directory:
```bash
mvn clean test
```
