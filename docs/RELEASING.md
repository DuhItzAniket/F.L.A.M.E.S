# Releasing

Versioning: SemVer. Development versions are `0.x.y`; the first stable
release is `1.0.0` (Phase 10 gate).

## What CI produces

Every push/PR to `main` runs `mvn -B verify` and uploads `target/flames-*.jar`
as the `flames-jar` artifact. The jar is a build artifact, not a standalone
distributable — JavaFX modules come from Maven Central at build time, so the
supported launch paths are below.

## Run from source (all platforms)

Requires JDK 21 + Maven 3.9+:

```sh
./mvnw javafx:run
```

## Versioned jar (verified)

```sh
./mvnw -B verify   # runs tests, produces target/flames-<version>.jar
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

## Release checklist

1. `docs/PHASE_STATUS.md` shows every phase COMPLETE.
2. `mvn -B verify` green on a clean checkout.
3. Version bumped in `pom.xml`; README status final.
4. `git tag v<version>` on the release commit; push with `git push --tags`.
5. Attach the CI jar (and installer once verified) to the GitHub release.
