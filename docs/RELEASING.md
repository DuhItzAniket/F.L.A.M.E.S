# Releasing

Versioning: SemVer. `1.0.0` is current; bump `pom.xml` + `package.bat`
`VERSION` together for the next feature milestone.

## What CI produces

Every push/PR to `main` runs `./mvnw -B clean verify` and uploads
`target/flames-*.jar` as the `flames-jar` artifact. The jar is a build
artifact, not a standalone distributable — the shippable is the
`package.bat` installer below.

## Run from source (all platforms)

Requires JDK 21 only (the wrapper bootstraps Maven 3.9.9):

```sh
./mvnw javafx:run   # mvnw.cmd on Windows; needs a display
```

## Versioned jar (verified)

```sh
./mvnw -B clean verify   # runs tests, produces target/flames-<version>.jar
```

## Windows installer (verified 1.0.0)

`package.bat` does the whole thing: Maven build, `jpackage` app-image,
then the exe installer. Needs JDK 21; the exe step needs WiX 3.x
`candle`/`light` on PATH (JDK 21 jpackage cannot use WiX 4+).

```sh
package.bat
```

Verified end to end: app-image exe stays up, exe installs per-user with
Start Menu group + desktop shortcut, installed exe stays up. The app is
installed at `%LocalAppData%\F.L.A.M.E.S` — no admin needed.

Notes from the trenches:

- The jpackage main class must be `flames.Launcher` (plain class), not
  `flames.MainApp`: an `Application` subclass makes the launcher demand
  modules on the module path and it exits with "JavaFX runtime
  components are missing".
- `target/libs` must contain the app jar too (`copy-dependencies` only
  stages dependencies) — `package.bat` copies it.
- Same-version reinstalls are refused (MSI 1638); uninstall first via
  `msiexec /x {ProductCode} /quiet` (the code is printed in a `/log`
  run) or bump the version.

## Release checklist

1. `docs/PHASE_STATUS.md` shows every phase COMPLETE.
2. `./mvnw -B clean verify` green on a clean checkout.
3. Version bumped in `pom.xml` + `package.bat`; README status final.
4. `git tag v<version>` on the release commit; push with `git push --tags`.
5. Create the GitHub release and attach the `package.bat` exe as a
   download asset (v1.0.0 lives at
   `https://github.com/DuhItzAniket/F.L.A.M.E.S/releases/tag/v1.0.0`).
