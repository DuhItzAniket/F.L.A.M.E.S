# Research: how FLAMES games are actually made (Sept 2026)

Sources are real pages fetched during research; claims below trace to them.
No AI features are proposed — nothing in this genre needs them, and the
app's offline promise forbids the network calls they'd require. GPU needs
nothing either: JavaFX Prism already uses Direct3D on Windows.

## The rules (ground truth)

- Wikipedia, "FLAMES (game)": cross out common letters pair by pair,
  count the remainder `n`, then either (a) Josephus-style elimination of
  F·L·A·M·E·S by `n`, or (b) the simpler variant — count `n` around FLAMES
  once and take the landing letter. Regional meaning variants exist
  (A = Affection/Admirer, S = Siblings/Secret Lovers). We implement (a);
  (b) is documented but not shipped (elimination *is* the classic).
- thecalculatorhive documents the exact normalization we use (lowercase,
  drop non-letters, cancel min-counts per letter) and the `m = 0`
  convention problem. Our zero-wrap rule stays, documented + tested.

## Feature catalog (what the popular ones do)

- Step-by-step breakdown: cancelled letters, count, elimination table
  (step | in play | removed | remaining) — thecalculatorhive, truelovecalc.
- Progress stepper: named steps (combine → cancel → count → eliminate) —
  radiusflow.
- Compatibility/bond meter from letter overlap — flamescalculator.io.
- Share: WhatsApp/Twitter links, copy-to-clipboard, save-card-as-image,
  screenshots — flamescalculator.io, truelovecalc.
- History of past games + clear — radiusflow.
- Manual mode: cross the pairs yourself, then reveal — truelovecalc.
- Group mode (3+ names) — flamescalculator.io. SKIPPED: no canonical
  rules, would invent semantics.
- Themes — mobixpro FLAMES app. Shipped (light/dark).
- Char counters (`n/50`) — radiusflow. Trivial, shipping.
- Fun disclaimers ("entertainment only") — every store listing. Shipping
  one line.
- Games-played counters, ratings, ads, accounts — web/mobile monetization
  clutter. Never shipping.

## Animation/graphics catalog

- Real-time strike-through as pairs cancel — flamesss (Vercel), Play
  Store "FLAMES Game" ("cool animations ... nostalgic feelings"). Shipped
  (pen slash).
- Particle fire (touch-drawn flames, fireplace mode) — "Fire in Phone
  Simulator" (100K+ downloads). Validates our ember direction.
- Result cards with big letter + meaning + share row — flamescalculator.io,
  radiusflow. Shipping (snapshot card).
- Elimination recap table — thecalculatorhive. Shipping on the result view.

## Packaging (proper application, not run.bat)

- JPackageScriptFX (dlemmermann, the standard recipe): non-modular app,
  jars on the classpath via `--input libs`, jpackage exe/msi with WiX on
  Windows, custom icon, menu + shortcuts. Following it exactly.
- JDK 21 jpackage needs WiX 3.x (WiX 4 needs JDK 24+). If WiX is absent,
  install WiX 3.14, then: app-image first (no WiX), exe second (WiX),
  install it, launch the installed exe as proof.
- No jmods on Maven Central, so no jlink custom runtime: bundle the
  default runtime via jpackage (documented limitation, standard practice).

## Implementation plan (phases, each verified + pushed)

- 22 (this doc): research + plan. Push.
- 23: elimination recap table + bond meter + char counters + stage
  stepper + disclaimer line. Push.
- 24: manual mode — clickable chips, user crosses pairs, count runs on
  what remains. Push.
- 25: local history (file store, view, replay, clear). Push.
- 26: result card PNG snapshot + copy-to-clipboard. Push.
- 27: WiX + jpackage exe; install; launch installed app. Push.
- 28: final gate + agentic scan. Push.
