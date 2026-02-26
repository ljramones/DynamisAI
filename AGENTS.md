# Repository Guidelines

## Project Structure & Module Organization
This repository is currently documentation-first.

- `docs/` contains the source specifications:
  - `DynamisAI_Master_Spec.docx`
  - `DynamisAI_Spec_v1.1.docx`
  - `DynamisAI_Architecture_NextSteps.docx`
- `.java-version` pins the local JDK version (`25`).

When code is added, use a standard Java layout:

- `src/main/java/...` for application code
- `src/test/java/...` for tests
- `docs/` remains the canonical product and architecture reference

## Build, Test, and Development Commands
There is no build system committed yet. Until one is added, use these baseline checks:

- `java -version` confirms the pinned runtime matches `.java-version`.
- `git status` verifies a clean working tree before commits.
- `ls docs` quickly validates required spec files are present.

If Maven or Gradle is introduced, document and use project wrapper commands (for example, `./mvnw test` or `./gradlew test`) in this file.

## Coding Style & Naming Conventions
For new Java code:

- Use 4-space indentation, UTF-8 files, and one public class per file.
- Package names: lowercase (example: `com.dynamis.ai.core`).
- Class names: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Prefer descriptive names tied to domain terms from `docs/`.

Run formatter/linter configured by the chosen build tool before opening a PR.

## Testing Guidelines
Adopt JUnit 5 for unit tests once the test module exists.

- Test files should end with `Test` (example: `PolicyEngineTest`).
- Mirror production package structure under `src/test/java`.
- Include happy-path, edge-case, and failure-path coverage for new logic.

## Commit & Pull Request Guidelines
Current history is minimal (`initial commit`), so follow a clear, consistent convention:

- Commit messages in imperative mood, concise subject line (example: `Add architecture decision record template`).
- Keep commits focused and logically scoped.
- PRs should include: summary, rationale, impacted paths, and validation steps.
- Link related issues/tasks and include screenshots only for UI/document rendering changes.

## Security & Configuration Tips
- Do not commit secrets, credentials, or environment-specific tokens.
- Keep large binaries out of the repo unless they are approved reference artifacts.
