# F.L.A.M.E.S — Development Plan

## 1. Project vision

A polished, modern Java desktop app inspired by the classic old-school FLAMES
(name-compatibility) game. Nostalgic at heart, contemporary in execution:
strong visual identity, intentional motion, custom icon/assets, clean
architecture, testable domain logic, reproducible builds. No network calls,
no tracking — fully offline, names never leave the machine.

## 2. Current repository state (Phase 0 audit, 2026-09-23)

- Local clone `F.L.A.M.E.S/` was **empty** (no source, build files, docs, assets).
- GitHub repo `DuhItzAniket/F.L.A.M.E.S` exists but is **empty**.
- No build system, no framework, no tests, no license, no CI.
- Toolchain found: JDK 21 LTS plus a system JDK 26 (unused — too new).
  No system Maven/Gradle at the time, so the Maven wrapper (`mvnw`) pins
  and bootstraps Maven 3.9.9; Maven Central + GitHub reachable.
- A stray `.git` exists in the Windows home directory with no commits; it is
  **not** this project and is left untouched. This project has its own `.git`.
- Tech debt: none (greenfield). License: none yet — owner to choose (suggest MIT).

## 3. Technology decisions (pinned, no floating versions)

| Concern | Choice | Why |
|---|---|---|
| Language | Java 21 LTS | Modern LTS; JavaFX 21 targets it |
| UI | JavaFX 21.0.4 (`org.openjfx`) | Visually rich desktop UI, CSS styling; owner's choice |
| Build | Maven 3.9.9, plugins pinned in `pom.xml` | Standard layout, reproducible, CI-friendly |
| Tests | JUnit 5.11.4 (via `junit-jupiter`) + Monocle headless views | Standard unit testing, UI tests without a display |
| CI | GitHub Actions, Ubuntu + Temurin 21 | `mvn -B verify` on push/PR |
| Assets | Generated PNG/ICO from `tools/IconGenerator.java` (pure Java2D) | Maintainable, no downloads, no licensing risk |
| Packaging (later) | `javafx-maven-plugin` 0.0.8 now; `jpackage` in Phase 8 | Defer native installers until UI is done |

Non-goals: no new dependencies beyond JavaFX/JUnit; no network code;
no analytics/telemetry.

## 4. Architecture proposal

```
flames/                  # single small package; split only when it hurts
  FlamesEngine.java      # pure domain: normalize → count → eliminate (no JavaFX)
  FlamesCategory.java    # enum F/L/A/M/E/S + titles + meanings
  FlamesOutcome.java     # record: category, count, elimination order, display names
  MainApp.java           # JavaFX entry point (presentation only, thin)
```

Rules: domain never imports JavaFX (testable headless); animation replays the
engine's elimination order — never a duplicate algorithm; UI stays thin.

## 5. Phase breakdown

- **Phase 0 — Audit & Architecture** (this doc). Done.
- **Phase 1 — Project Foundation.** Maven build, JavaFX shell that launches,
  CI workflow, `.gitignore`, README + docs skeleton. *Done when: `mvn verify`
  green, window opens, CI configured.*
- **Phase 2 — Domain / Game Engine.** `FlamesEngine` + categories + outcome +
  edge cases + JUnit suite green. *Done when: all algorithm tests pass,
  edge cases (empty, identical, unicode, punctuation) covered + documented.*
- **Phase 3 — Application UI.** Name inputs, calculate action, result view,
  restart/retry flow, error states. Real functionality only.
- **Phase 4 — Visualization & Animation.** Elimination sequence driven by the
  engine's recorded order; result reveal.
- **Phase 5 — Visual Identity & Assets.** Palette, typography, custom SVG
  assets, app icon + variants, `docs/DESIGN_SYSTEM.md`.
- **Phase 6 — UX / Interaction Polish.** Focus states, keyboard flow, timing,
  empty/error state polish.
- **Phase 7 — Testing, Reliability & Accessibility.** Coverage of new paths,
  regression tests, contrast/keyboard audit.
- **Phase 8 — Packaging & Release.** `jpackage` installers, versioned release.
- **Phase 9 — Documentation & Repo Polish.** Screenshots, final README,
  dead-code/asset sweep.
- **Phase 10 — Final Quality Gate.** Full verify, manual walkthrough, tag release.

## 6. Testing strategy

- JUnit 5 unit tests for the engine: normalization, counts, known outcomes,
  elimination-order invariants, every edge case in §8 of the directive.
- Regression tests added alongside every bug fix.
- Headless-safe: domain tests never start the JavaFX toolkit.
- UI verified by real launch smoke tests where a display exists.
- CI runs the full suite; suite must stay fast (< 1 min).

## 7. Documentation strategy

Update docs in the same commit as the code. Keep: `README.md`,
`docs/DEVELOPMENT_PLAN.md` (this file), `docs/ARCHITECTURE.md`,
`docs/TESTING.md`, `docs/PHASE_STATUS.md`; add `docs/DESIGN_SYSTEM.md` in
Phase 5 and an algorithm note with Phase 2.

## 8. Asset strategy

SVG sources kept in `src/main/resources/assets/`, PNG renders only where the
platform needs them. Every asset referenced by the app or deleted. Icon
designed in Phase 5 with small-size legibility as acceptance criterion.

## 9. Release strategy

`main` always builds green. No release until Phase 10. Version `0.x.y`
during development; `1.0.0` at the final gate with `jpackage` artifacts.

## 10. Definition of done (every phase)

Code + tests + docs updated together; `mvn -B verify` green; diff reviewed;
no TODOs for core behavior; no dead files/assets; committed with a
Conventional Commit message; `docs/PHASE_STATUS.md` updated; pushed only
after all of the above.
