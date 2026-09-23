# Testing

- Framework: JUnit 5 (`junit-jupiter`, pinned in `pom.xml`).
- Run: `mvn verify` (JDK 21). Full suite must stay under a minute.
- Domain tests (`FlamesEngineTest`) cover: normalization (case, spaces,
  punctuation, digits, Unicode), cross-cancellation counts incl. repeats,
  a hand-traced outcome (`john`/`jane` → Enemies, order M·L·F·A·S),
  the zero-count rule (identical names → Marriage), elimination invariants
  (5 removals, survivor stands apart), determinism, display-name
  preservation, and rejection of blank/punctuation-only/null inputs with
  messages naming the offending field.
- Rule: every bug fix ships with a regression test. Never delete a test
  because it is inconvenient.
- UI smoke tests are manual until Phase 7 (`mvn javafx:run` needs a display).
- CI (`.github/workflows/ci.yml`) runs `mvn -B verify` on push/PR to `main`.
