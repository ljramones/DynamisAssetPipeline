# DynamisAssetPipeline Architecture Review

Date: 2026-03-09  
Scope: Deep boundary ratification for `DynamisAssetPipeline` (review/documentation only)

## 1. Repo Overview

Observed modules:

- `pipeline-api`
- `pipeline-core`
- `pipeline-cli`
- `pipeline-test`

Observed implementation shape:

- `pipeline-api` defines cook request/result and manifest output contracts (`MeshCookRequest`, `MeshCookResult`, `ManifestOut`, `ManifestEntryOut`).
- `pipeline-core` performs mesh cooking using MeshForge (`MeshCooker`) and writes a deterministic cooked container (`DmeshWriter` / `DmeshReader` / `DmeshFile`).
- `pipeline-cli` provides argument parsing and manifest merge/write behavior (`MeshCookCli`, `ManifestWriter`).
- `pipeline-test` validates deterministic output and manifest behavior.

Current dependency signal from poms/code:

- `pipeline-core` depends on MeshForge artifacts (`meshforge`, `meshforge-loader`) and pipeline API.
- No direct dependencies on world/session/render/runtime engine repos were found.
- API isolation test explicitly guards against `EntityId`/`vectrix` leakage in API.

## 2. Strict Ownership Statement

### What DynamisAssetPipeline should own

- Build-time/import-time/bake-time asset transformation orchestration.
- Pipeline-stage contracts and deterministic cooked-artifact generation.
- Build-time manifest emission for produced cooked artifacts.
- Versioning and format discipline for pipeline outputs (for example `mesh.packed.dmesh.v0` and DMESH file envelope).

### What is appropriate for this repo right now

- Source mesh ingest and conversion through MeshForge into cooked mesh payload output.
- Deterministic output guarantees and reproducible cooking behavior tests.
- CLI/tooling for running the cook and generating/updating output manifests.

### What it must never own

- Runtime content lookup/resolution/cache authority (belongs with DynamisContent/runtime layers).
- Session/save-load authority (belongs with DynamisSession).
- World authority/tick orchestration (belongs with DynamisWorldEngine).
- Renderer planning or GPU execution ownership (belongs with DynamisLightEngine/DynamisGPU).
- Gameplay/scripting policy ownership.

## 3. Dependency Rules

### Allowed dependencies for DynamisAssetPipeline

- Foundation libraries/utilities and build tooling.
- Specialized build-time transformers such as MeshForge for geometry preparation.
- Format serialization libs needed for build outputs (for example Jackson in CLI).

### Forbidden dependencies for DynamisAssetPipeline

- Runtime-authority repos: world/session/render planning/GPU execution.
- Runtime content authority implementations (it may emit artifacts consumed by them, but should not own runtime resolution behavior).
- Feature-policy repos (gameplay/scripting/session policy).

### Who may depend on DynamisAssetPipeline

- Build tools/authoring tooling/CI cook steps.
- Asset production workflows and offline preparation pipelines.

### Who should not depend on it directly at runtime

- Runtime gameplay loops, world tick orchestration, render loops.
- Runtime content access should consume cooked artifacts/contracts, not invoke pipeline cooking logic in-frame.

## 4. Public vs Internal Boundary

### Canonical public surface (recommended)

- `pipeline-api` records/contracts:
  - `MeshCookRequest`, `MeshCookResult`
  - `ManifestOut`, `ManifestEntryOut`
  - stable primitive value types (`AssetPath`, `CookProfile`, `CookedAssetUri`)

### Internal/implementation surface (should remain internal)

- `pipeline-core` implementation classes:
  - `MeshCooker`
  - `DmeshWriter`, `DmeshReader`, `DmeshFile`
- `pipeline-cli` concrete tooling:
  - `MeshCookCli`
  - `ManifestWriter`

### Boundary concern

- `pipeline-core` currently exposes concrete implementation types in exported packages because module-level encapsulation boundaries are not explicit in this repo layout. This is acceptable now, but treat core/cli classes as implementation details and avoid freezing them as long-term stable API.

## 5. Policy Leakage / Overlap Findings

## Clean boundaries confirmed

- Clear build-time orientation: mesh cooking + deterministic output generation.
- No observed world/session/render/GPU orchestration ownership.
- MeshForge is used as geometry-preparation specialist rather than reimplemented.
- Tests enforce deterministic cooking and minimal API leakage.

## Overlap / risk candidates

- **DynamisContent overlap risk (watch):** `ManifestOut`/`ManifestEntryOut` represent output references that resemble runtime catalog semantics. Keep these as pipeline-output descriptors, not runtime content authority contracts.
- **MeshForge overlap risk (watch):** pipeline currently couples to MeshForge pipelines/packers and additionally wraps output in `DMESH`. Ensure MeshForge remains geometry-prep specialist while AssetPipeline owns only orchestration/output packaging.
- **Runtime invocation risk (watch):** no current runtime policy leakage is visible, but avoid letting CLI/core become runtime loader/resolver codepaths.
- **Scope narrowness risk (watch):** current implementation is mesh-focused; avoid broadening with feature/runtime orchestration under this repo name.

## 6. Ratification Result

**Judgment: ratified with constraints**

Why:

- The current code is strongly build-time focused and largely clean on subsystem boundaries.
- Dependencies point downward to preparation tooling (MeshForge), not upward into runtime authorities.
- Main risk is future drift around manifest semantics and accidental runtime usage; constraints are clear and manageable.

## 7. Recommended Next Step

1. Keep AssetPipeline boundary as build/import/bake authority only.
2. During future reviews, ratify the **AssetPipeline ↔ Content** handoff contract explicitly so runtime catalog authority stays in DynamisContent.
3. Next repo to review: **DynamisLocalization** (from the remaining overlap-prone runtime-data side), unless the planned sequence prioritizes another unresolved authority boundary first.

---

This document is a boundary-ratification review artifact. It does not propose refactors in this pass.
