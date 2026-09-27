# Testing

- Framework: JUnit 5 (`junit-jupiter`, pinned in `pom.xml`).
- Run: `./mvnw -B clean verify` (JDK 21 only; the wrapper bootstraps
  Maven). Full suite must stay under two minutes (one headless
  play-advance test sleeps ~4.5 s).
- Domain tests (`FlamesEngineTest`, 22 tests) cover: normalization (case,
  spaces, punctuation, digits, Unicode), cross-cancellation counts incl.
  repeats, a hand-traced outcome (`john`/`jane` → Enemies, order M·L·F·A·S),
  the zero-count rule (identical names → Marriage), elimination invariants
  (5 removals, survivor stands apart), determinism, display-name
  preservation, rejection of blank/punctuation-only/null inputs with
  messages naming the offending field, plus the newer surface:
  `cancellationOrder` (incl. count relation), `eliminationRounds`
  (hand-traced, negative rejected), `bondPercent` (0/50/100),
  explicit-count override (incl. negative rejected), and category
  content/letter checks.
- Rule: every bug fix ships with a regression test. Never delete a test
  because it is inconvenient.
- View tests (`ViewTest`, 20 tests) run headless via Monocle
  (`org.testfx:openjfx-monocle`, test scope; Glass/Prism properties in the
  surefire config): error show/clear cycle, submit wiring with both names,
  manual-mode flag, verdict content incl. the zero-count caption, recap
  rows + bond value, six-tile initial layout, chip layout, manual
  click-through (mismatch guidance, cross, count, done), fail-fast
  construction on non-FLAMES letters, both themes shipping, theme class
  parity, ember bursts, verdict reveal, planned survivors, play-advance
  (pins timeline progress headless), and skip-path end states
  (exactly-once handoff, dropped tiles, completed slashes — incl.
  skip-right-after-play, the regression test for a once-fatal
  double-cancel bug). Animation feel stays a manual eyes-on check.
- Store tests (`SettingsTest` 5, `HistoryStoreTest` 5): preference
  round-trips incl. clamping/normalization, history cap/order/clear,
  corrupt-line tolerance.
- Rendered-UI verification beyond that is manual (`run.bat` needs eyes).
- CI (`.github/workflows/ci.yml`) runs `./mvnw -B clean verify` on push/PR
  to `main`.

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
