# F.L.A.M.E.S

A modern JavaFX revival of the classic old-school FLAMES name game.
Enter two names, watch the letters fall, get your verdict:
**Friends, Love, Affection, Marriage, Enemies, Siblings.**

Fully offline — names never leave your machine.

## Status

Phase 3 done: name entry, verdict view, and inline error states.
Elimination animation lands in Phase 4.
See `docs/PHASE_STATUS.md` and `docs/DEVELOPMENT_PLAN.md`.

## Prerequisites

- JDK 21 (LTS)
- Maven 3.9+

## Build, test, run

```sh
mvn verify          # compile + full test suite
mvn javafx:run      # launch the app (needs a display)
```

## How it works

1. Names are lowercased; only letters are kept.
2. Shared letters cancel out; the leftovers are counted.
3. The count eliminates letters from F·L·A·M·E·S in a circle until one survives.
4. Fully cancelling names (e.g. identical) wrap to one full cycle.

Details in `docs/ARCHITECTURE.md` and `docs/TESTING.md`.

## Project structure

```
pom.xml
src/main/java/flames/      # engine, views, entry point
src/main/resources/assets/ # flames.css, icon/ (PNGs + ICO)
src/test/java/flames/      # JUnit suite
tools/                     # IconGenerator (artwork source)
docs/                      # plan, architecture, design, testing, phase status
.github/workflows/         # CI
```
