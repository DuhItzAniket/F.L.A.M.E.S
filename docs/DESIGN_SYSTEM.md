# Design system — "ember notebook"

Nostalgic medium: warm paper, ink, a rubber-stamp verdict. Modern manners:
consistent spacing, one accent, short motion. If it isn't listed here, it
doesn't ship.

## Palette

| Token | Hex | Use |
|---|---|---|
| paper | `#F7F0E1` | app background |
| card | `#FFFDF6` | fields, tiles |
| ink | `#2B2118` | text, tile borders |
| ink-soft | `#6B5D4F` | subtitles, status, meanings |
| faint | `#75644E` | prompts, hints, fallen tiles |
| line | `#D8C9AE` | field borders |
| ember | `#C93A2E` | hover, highlights (large areas only) |
| ember-deep | `#A02A22` | primary buttons, verdict word, winner tile |
| ember-dark | `#8E231C` | icon gradient base |
| gold | `#D9A441` | focus rings; icon flame-drop `#E8B54A` |

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

- Elimination step 500 ms; crowning pause 700 ms; handoff 900 ms.
- No loops, no confetti, click anywhere skips. Every animated step also
  writes a status sentence, so muting motion loses nothing.

## Icon & assets

- Mark: ember rounded-square tile, cream heart, gold flame-drop. Bold
  silhouette, legible at 16 px, no text, no tiny detail.
- Source: `tools/IconGenerator.java` (pure Java2D, headless, no deps).
  Generated files are checked in under `src/main/resources/assets/icon/`:
  `icon-16/32/48/128/256.png` + `flames.ico` (PNG-compressed, 5 images).
- Window icons: 16/32/48. In-app logo: 128 on the input view.
- Styling: `src/main/resources/assets/flames.css`. Every asset is referenced
  by the app; there are no spare graphics.
