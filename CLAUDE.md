# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is an **OpenAPI spec-only** repository for the HMCTS API Marketplace (`api-cp-*` naming family). It does not implement business logic — it defines a single OpenAPI 3.0 contract and uses it to generate Spring interfaces and models that are consumed as a published Maven artifact by a separate `service-cp-*` Spring Boot service. There is exactly one spec per repo.

The repo currently still contains template placeholder content (`ExampleResponse`/`ExamplesApi`, `src/main/resources/openapi/schema/deleteme.md`) carried over from `api-hmcts-crime-template` — real domain schemas/paths for case admin / case management have not yet replaced the example content.

## Commands

- Build (regenerates code from spec, compiles, runs PMD-gated checks): `./gradlew build`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "uk.gov.hmcts.cp.config.OpenApiObjectsTest"`
- Jacoco coverage report (runs as part of `check`): `./gradlew jacocoTestReport`
- PMD static analysis (only runs when explicitly invoked — not wired into `build`/`check` by default): `./gradlew pmdMain` (ruleset: `.github/pmd-ruleset.xml`)
- Lint the OpenAPI spec with Spectral (same tool CI runs): `npx @stoplight/spectral-cli lint "src/main/resources/openapi/*.{yml,yaml}"` (config: `.spectral.yml`, extends `spectral:oas`)
- Regenerate OpenAPI-derived sources only: `./gradlew openApiGenerate` (output lands in `build/generated/src/main/java`, which is auto-added as a source dir and rebuilt as part of `compileJava`)

There is no `main` application — `jar` packages the generated API interfaces/models plus a CycloneDX SBOM and `CHANGELOG.md` (if present) for consumption as a library.

## Architecture

- **The single source of truth is `src/main/resources/openapi/openapi-spec.yml`.** Everything else in `src/main/java`/`build/generated` is derived from it — never hand-edit generated classes under `uk.gov.hmcts.cp.openapi.*`; edit the spec and regenerate instead.
- Code generation (`gradle/openapi.gradle`, `openapi-generator` Gradle plugin, generator `spring`):
  - `interfaceOnly: true` — only Spring controller **interfaces** are generated (`uk.gov.hmcts.cp.openapi.api`), no concrete controllers. The consuming `service-cp-*` implements these interfaces.
  - Models land in `uk.gov.hmcts.cp.openapi.model`, annotated with Lombok `@Builder`/`@AllArgsConstructor`/`@NoArgsConstructor` via `additionalModelTypeAnnotations`.
  - `OffsetDateTime` is remapped to `java.time.Instant` throughout (`typeMappings`/`importMappings`).
- **Schemas must be defined inline** under `components.schemas` in the spec. Do not `$ref` external JSON Schema files — per `docs/OPENAPI-FILE-CONVENTIONS.md` they render incorrectly in Swagger UI, even though the generator technically supports it.
- `info.version` in the spec is a static placeholder (currently `1.0.0`/`0.0.0`-style) — the real version is injected by CI (`hmcts/update-openapi-version` action) based on short-SHA (draft, on push to main/master) or the GitHub release tag (release). Don't hand-maintain this field as the real version. See `docs/OPENAPI-SPEC-VERSIONING.md`.
- The only tests in the repo (`src/test/java/.../config/OpenApiObjectsTest.java`) are contract-shape assertions against the *generated* interfaces/models (e.g. asserting `ErrorResponse` has an `Instant timestamp` field, `RootApi`/`ExamplesApi` expose the expected methods). When the spec changes, update these tests to match the new generated shape rather than treating them as fixed behavioural tests.
- `gradle/*.gradle` files are split by concern and all applied from `build.gradle`: `repositories.gradle` (Maven Central + Azure DevOps Artifacts feed + GitHub Packages/Azure publishing), `java.gradle` (Java 25 toolchain, `-Werror`), `dependency.gradle` (stable-version-only dependency updates check, release use only), `pmd.gradle`, `test.gradle` (JUnit 5, JaCoCo wired into `check`), `jar.gradle`, `openapi.gradle`.
- CI (`.github/workflows/`): `lint-openapi.yml` runs on every PR (disallowed-domain check for internal HMCTS URLs, JSON schema lint/validate for any `schema/*.json` + matching `*.example.json` pairs, Spectral). `ci-draft.yml` builds/tests/publishes a draft artifact on push/PR to main/master; `ci-released.yml` does the same on GitHub release publish. `code-analysis.yml` runs PMD as a separate required PR check (unlike the local `pmdMain` task, this always runs).

## Repository naming convention (for context when referencing other repos)

```
api-{source-system}-[case-type]-{business-domain}-{entity-name}
```
e.g. this repo, `api-cp-crime-caseadmin-case-management`: source system `cp` (Common Platform), case-type `crime`, business domain `caseadmin`, entity `case-management`. Reference-data APIs instead follow `api-cp-refdata-{product-domain}-{entity-name}`. Avoid ambiguous-ownership names (`common`, `core`, `base`, `utils`, `shared`, etc.) in any new repo or module names.
