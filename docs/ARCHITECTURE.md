# Architecture

Small app, small architecture. One package (`flames`), three layers by
dependency direction:

- **Domain** — `FlamesEngine`, `FlamesCategory`, `FlamesOutcome`.
  Pure Java, zero JavaFX imports. All game rules live here and only here.
- **Presentation** — `MainApp` (+ Phase 3 views). Thin: reads input, calls
  the engine, renders the outcome. Never reimplements elimination.
- **Infrastructure** — Maven build, CI workflow, (Phase 8) packaging.

Key decisions:

- The engine records the elimination order so animation replays the real
  calculation (`FlamesOutcome.eliminationOrder`). No second implementation.
- Normalization keeps Unicode letters, drops everything else (spaces,
  punctuation, digits, symbols). Documented in `FlamesEngine`.
- A remaining count of 0 (fully cancelling names, e.g. identical) wraps to
  one full 6-cycle instead of special-casing a winner. Deterministic,
  covered by test.
- No `module-info.java` by decision: the app ships via `javafx-maven-plugin`
  (dev) and a `jpackage` command over the plain jar (release). A module
  descriptor would add friction with zero benefit at this size; the
  `maven-jar-plugin` manifest (`Main-Class` + classpath) is as far as the
  jar needs to go.
