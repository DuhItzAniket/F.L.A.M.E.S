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
mvn javafx:run
```

## Versioned jar (verified)

```sh
mvn -B verify   # runs tests, produces target/flames-<version>.jar
```

## Windows installer (not yet verified here — needs WiX on the build machine)

```sh
mvn -B -DskipTests package
jpackage --type exe --name F.L.A.M.E.S --app-version 1.0.0 \
  --input target --main-jar flames-1.0.0.jar \
  --main-class flames.MainApp --icon src/main/resources/assets/icon/flames.ico \
  --win-menu --win-shortcut
```

Do not publish an installer built any other way.

## Release checklist

1. `docs/PHASE_STATUS.md` shows every phase COMPLETE.
2. `mvn -B verify` green on a clean checkout.
3. Version bumped in `pom.xml`; README status final.
4. `git tag v<version>` on the release commit; push with `git push --tags`.
5. Attach the CI jar (and installer once verified) to the GitHub release.
