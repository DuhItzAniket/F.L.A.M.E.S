# F.L.A.M.E.S

A modern JavaFX revival of the classic old-school FLAMES name game.
Enter two names, watch the letters fall one by one, get your verdict:
**Friends, Love, Affection, Marriage, Enemies, Siblings.**

Fully offline — names never leave your machine. No accounts, no tracking,
no network calls.

## How it plays

1. Type two names and hit **Reveal fate** (or Enter) — tick
   **Cross the pairs myself** to cross them out by hand instead.
2. Shared letters are slashed pair by pair, pen-on-paper style.
3. The survivors light up gold as they are counted aloud, one by one.
4. The count hops around the F·L·A·M·E·S ring, ticking as it goes; each
   fallen letter is slashed, shakes, and drops away — click or press
   Enter to skip. The stepper up top tracks Names → Cross out → Count.
5. The verdict appears with its meaning, a bond meter, and a 5-row
   recap of exactly how it happened. **Change names** keeps your input,
   **Start over** clears it, **Save card** exports a PNG, **Copy result**
   copies the one-liner.
6. The gear button (top right) holds settings: theme, sound effects,
   volume, and ambient embers. **History** (top left) replays past games.

Only letters count; case, spaces, punctuation, and digits are ignored.
Names that cancel out completely (like identical names) wrap to one full
counting cycle.

## Run it

**Easiest:** download **`F.L.A.M.E.S-1.0.0.exe`** from the
[v1.0.0 release](https://github.com/DuhItzAniket/F.L.A.M.E.S/releases/tag/v1.0.0),
double-click to install, and launch from the Start Menu. No Java needed.
(Windows SmartScreen may warn because the installer is unsigned —
More info / Run anyway.)

From source — requires JDK 21 only, the wrapper bootstraps Maven:

```sh
mvnw.cmd javafx:run   # Windows; ./mvnw javafx:run on Unix (needs a display)
mvnw.cmd verify       # full test suite, headless-safe
```

Shortcuts: double-click **`run.bat`** to play, **`package.bat`** to build
the Windows installer (needs WiX 3.x for the exe step).

## Under the hood

- `FlamesEngine` — pure-Java game logic (normalize → cancel → eliminate),
  independently tested, zero UI imports. Exposes `cancellationOrder`,
  `eliminationRounds` (the single source behind the verdict, the recap,
  and the animation tripwire), `bondPercent`, and a manual-count
  overload for cross-it-yourself play.
- `EliminationView` replays engine data with pen slashes, count-up,
  hop counting, and ember bursts, so the animation *is* the
  calculation, not a lookalike.
- "Ember notebook" visual system: warm paper, ink text, one ember accent.
  Tokens in `docs/DESIGN_SYSTEM.md`; custom app icon generated from
  `tools/IconGenerator.java` (no downloaded graphics).

## Project structure

```
pom.xml                            # pinned: JavaFX 21.0.2, JUnit 5.11.4
src/main/java/flames/              # engine + views + settings + entry point
src/main/resources/assets/         # flames.css, flames-dark.css, sound/,
                                   # fonts/, icon/ (PNGs + ICO)
src/test/java/flames/      # 52 tests: 22 engine + 20 view + 5 settings + 5 history
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
- `docs/RELEASING.md` — versioning, installer, and release checklist
- `docs/RESEARCH.md` — genre research and references behind phases 23–27

## Status & license

All phases 0–27 complete (see `docs/PHASE_STATUS.md`), each verified
with tests, live launches, and an agentic deep scan at every gate.
Eyes-on feel (motion, embers, sound balance, saved-card pixels) is the
standing manual check; everything else is CI-verified.
License: to be chosen by the repository owner.
