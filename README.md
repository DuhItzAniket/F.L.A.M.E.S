# F.L.A.M.E.S

A modern JavaFX revival of the classic old-school FLAMES name game.
Enter two names, watch the letters fall one by one, get your verdict:
**Friends, Love, Affection, Marriage, Enemies, Siblings.**

Fully offline — names never leave your machine. No accounts, no tracking,
no network calls.

## How it plays

1. Type two names and hit **Reveal fate** (or Enter).
2. Shared letters cancel out; the leftovers count around the
   F·L·A·M·E·S tiles until one survives — click or press Enter to skip.
3. The verdict appears with its meaning. **Change names** keeps your input,
   **Start over** clears it.

Only letters count; case, spaces, punctuation, and digits are ignored.
Names that cancel out completely (like identical names) wrap to one full
counting cycle.

## Run it

Requires JDK 21 (LTS). The Maven wrapper bootstraps Maven 3.9.9 itself —
no preinstalled Maven needed:

```sh
./mvnw javafx:run      # launch the app (needs a display; mvnw.cmd on Windows)
./mvnw verify          # compile + full test suite (headless-safe)
```

Windows manual test shortcut: double-click **`run.bat`** (uses JDK 21 if
present, otherwise your `JAVA_HOME`).

## Under the hood

- `FlamesEngine` — pure-Java game logic (normalize → cancel → eliminate),
  independently tested, zero UI imports.
- `EliminationView` replays the engine's recorded elimination order, so the
  animation *is* the calculation, not a lookalike.
- "Ember notebook" visual system: warm paper, ink text, one ember accent.
  Tokens in `docs/DESIGN_SYSTEM.md`; custom app icon generated from
  `tools/IconGenerator.java` (no downloaded graphics).

## Project structure

```
pom.xml                            # pinned: JavaFX 21.0.4, JUnit 5.11.4
src/main/java/flames/              # engine + views + entry point
src/main/resources/assets/         # flames.css, icon/ (PNGs + ICO)
src/test/java/flames/      # 16 engine tests + 7 headless view tests
tools/IconGenerator.java           # artwork source (pure Java2D)
docs/                              # plan, architecture, design, testing,
                                   # phase status, releasing
.github/workflows/ci.yml           # build + test + jar artifact
```

## Docs

- `docs/DEVELOPMENT_PLAN.md` — vision, decisions, phases
- `docs/ARCHITECTURE.md` — structure and key rules
- `docs/DESIGN_SYSTEM.md` — palette, type, motion, assets
- `docs/TESTING.md` — strategy plus the measured accessibility audit
- `docs/PHASE_STATUS.md` — per-phase verification log
- `docs/RELEASING.md` — versioning and release checklist

## Status & license

All phases 0–10 complete; `1.0.0` tagged `v1.0.0`, followed by a deep-scan
hardening pass (bundled font, Maven wrapper, headless view tests —
see `docs/PHASE_STATUS.md`). The one check that needs a display is the
on-screen walkthrough; everything else is CI-verified.
License: to be chosen by the repository owner.
