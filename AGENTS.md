# Repository Guidelines

## Project Structure & Module Organization
This repository is currently a minimal scaffold. At the moment, tracked files are:

- `.java-version` (Java runtime pin, currently `25`)
- `.git/` metadata

As code is added, keep a predictable layout:

- `src/` for production code
- `tests/` for automated tests
- `assets/` for static pipeline inputs (sample files, fixtures)
- `docs/` for design notes and operational guides

Use small, focused modules and group files by feature or pipeline stage.

## Build, Test, and Development Commands
No build system is configured yet (`pom.xml`, `build.gradle`, or `Makefile` are not present). Until tooling is introduced, use:

- `git status` to confirm working tree changes
- `git log --oneline` to review recent commit history

When build/test tooling is added, document canonical commands here (for example: `./gradlew test` or `mvn test`) and keep them stable for contributors and CI.

## Coding Style & Naming Conventions
Follow these defaults until language-specific formatters are added:

- Use 4 spaces for indentation (no tabs)
- Use descriptive, domain-specific names (`asset_manifest`, `pipeline_stage`)
- Prefer lowercase `kebab-case` for file names where language permits
- Keep classes/types in `PascalCase`, methods/variables in `camelCase`

If a formatter/linter is introduced, treat its output as authoritative and run it before opening a PR.

## Testing Guidelines
No test framework is configured yet. When adding tests:

- Mirror source structure under `tests/`
- Name test files to match source modules (`<module>.test.*` or equivalent)
- Cover happy-path and failure-path pipeline behavior

Add reproducible fixtures under `assets/fixtures/` when tests require sample data.

## Commit & Pull Request Guidelines
Current history uses short, imperative commit messages (example: `initial`). Continue with:

- One-line imperative subject (`add asset manifest parser`)
- Optional body for rationale and tradeoffs

For pull requests, include:

- What changed and why
- How to validate locally
- Related issue/task reference
- Sample input/output notes for pipeline-impacting changes

## Asset Pipeline v0 Policy
- This project is a build-time asset preparation tool and is not loaded at runtime.
- All cooked outputs and manifest files must be deterministic across repeated runs.
- The pipeline remains renderer-agnostic; no renderer-specific runtime assumptions in formats.
- Scope for v0 is mesh-only cooking and manifest emission compatibility.
