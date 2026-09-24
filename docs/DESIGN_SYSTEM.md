# Design system — "ember notebook"

Nostalgic medium: warm paper, ink, a rubber-stamp verdict. Modern manners:
consistent spacing, one accent, short motion. If it isn't listed here, it
doesn't ship.

## Palette

Light (`flames.css`) — paper `#F7F0E1`, card `#FFFDF6`, ink `#2B2118`,
ink-soft `#6B5D4F`, faint `#75644E`, line `#D8C9AE`, ember `#C93A2E`,
ember-deep `#A02A22`.

Dark "ember night" (`flames-dark.css`) — bg `#231B14`, card `#31271C`,
cream `#F4E9D2`, soft `#C9B795`, faint `#B9A888`, line `#4E4132`,
ember `#E0604F` (large text/hover states), ember-deep `#A02A22` (buttons),
hover `#B23A2E`. Shared: gold `#E8B54A` focus, icon flame-drop `#E8B54A`.

Measured dark pairs: cream/bg 14.1, soft/bg 8.6, cream/button 6.1,
cream/hover 4.9, ember-word/bg 4.8 (large), faint/bg 7.3, error/bg 4.8.
Both files define identical class sets (enforced by test).

One accent (ember), one signal (gold). Errors reuse `ember-deep` + words —
never color alone.

## Typography

- Display: bundled **Gentium Book Plus** (SIL OFL — TTFs + license in
  `assets/fonts/`), registered in code by `MainApp` — title 46, verdict
  word 64, names line 22, tiles 30. Same look on every machine, still
  fully offline. (JavaFX `@font-face` cannot express the bold face and its
  parser rejects the descriptor, so CSS only names the family; if loading
  ever fails, the platform default renders instead.)
- UI: platform default sans — labels 14, fields 16, buttons 15, status 15,
  hints 13. Always available, always offline.

## Spacing, shape, depth

- Scale: 8 · 12 · 16 · 24 · 32. View padding 32, view gaps 12–16, tiles gap 10.
- Radius 10 everywhere (CSS `-fx-background-radius` + `-fx-border-radius`).
- One shadow only: focused field glows gold. Nothing else floats.

## Motion

- Cancellation: shared-letter pairs fade out (280 ms) at up to 450 ms
  cadence (compressed to fit 3 s for long names), counter narrates.
- Counting: highlight hops the ring at up to 110 ms cadence (compressed
  to fit 1.1 s per elimination), tick per hop; the landed tile punches
  (scale 1.28) and pops.
- Crowning pulse 320 ms; handoff 900 ms. Click/Enter/Space skips to the
  verdict. Every step writes a status sentence, so muting motion (or
  sound) loses nothing.

## Icon & assets

- Mark: ember rounded-square tile, cream heart, gold flame-drop. Bold
  silhouette, legible at 16 px, no text, no tiny detail.
- Source: `tools/IconGenerator.java` (pure Java2D, headless, no deps).
  Generated files are checked in under `src/main/resources/assets/icon/`:
  `icon-16/32/48/128/256.png` + `flames.ico` (PNG-compressed, 5 images).
- Window icons: 16/32/48. In-app logo: 128 on the input view.
- Styling: `src/main/resources/assets/flames.css`. Every asset is referenced
  by the app; there are no spare graphics.
