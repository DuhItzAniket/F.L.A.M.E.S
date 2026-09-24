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
- View tests (`ViewTest`, 7 tests) run headless via Monocle
  (`org.testfx:openjfx-monocle`, test scope; Glass/Prism properties in the
  surefire config): error show/clear cycle, submit wiring with both names,
  verdict content incl. the zero-count caption, six-tile initial layout,
  and fail-fast construction on non-FLAMES letters. Animation timing stays
  a manual on-display check.
- UI smoke tests are manual until Phase 7 (`mvn javafx:run` needs a display).
- CI (`.github/workflows/ci.yml`) runs `mvn -B verify` on push/PR to `main`.

## Accessibility audit (Phase 7, measured)

Contrast (WCAG 2.1, normal text needs 4.5:1, large text 3:1):

| Pair | Ratio | Verdict |
|---|---|---|
| ink on paper (body) | 13.9 | AAA pass |
| ink-soft on paper (subtitles, meanings) | 5.6 | AA pass |
| cream on ember-deep (buttons, verdict word) | 7.0 | AAA pass |
| cream on ember (button hover) | 4.8 | AA pass |
| faint `#75644E` on paper (prompts, hints) | 5.0 | AA pass — darkened from `#A2937E` (2.6, fail) |
| ember-deep on paper (errors, 14px bold) | 6.5 | AA pass |

Non-color cues: every elimination step writes a status sentence and updates
screen-reader text; errors are words, not just red; the verdict is a word,
not just a color. Keyboard map: Tab follows fields → actions; Enter submits
(input) / goes back (result) / skips (animation); Space also skips; focus is
moved into the first field on every return to input.
