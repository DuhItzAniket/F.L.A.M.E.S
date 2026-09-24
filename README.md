# F.L.A.M.E.S

A modern JavaFX revival of the classic old-school FLAMES name game.
Enter two names, watch the letters fall one by one, get your verdict:
**Friends, Love, Affection, Marriage, Enemies, Siblings.**

Fully offline — names never leave your machine. No accounts, no tracking,
no network calls.

## How it plays

1. Type two names and hit **Reveal fate** (or Enter).
2. Shared letters are slashed pair by pair, pen-on-paper style.
3. The survivors light up gold as they are counted aloud, one by one.
4. The count hops around the F·L·A·M·E·S ring, ticking as it goes; each
   fallen letter is slashed, shakes, and drops away — click or press
   Enter to skip.
5. The verdict appears with its meaning. **Change names** keeps your input,
   **Start over** clears it.
6. The gear button (top right) holds settings: theme, sound effects,
   and volume.

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
pom.xml                            # pinned: JavaFX 21.0.2, JUnit 5.11.4
src/main/java/flames/              # engine + views + settings + entry point
src/main/resources/assets/         # flames.css, flames-dark.css, sound/,
                                   # fonts/, icon/ (PNGs + ICO)
src/test/java/flames/              # 34 tests: 18 engine + 12 view + 4 settings
tools/                     # IconGenerator, SoundGenerator (asset sources)
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

All phases 0–17 complete (see `docs/PHASE_STATUS.md`), followed by a
hardening pass with an agentic deep scan. The one check that needs eyes
is motion/sound/theme feel; everything else is CI-verified.
License: to be chosen by the repository owner.
