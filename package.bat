@echo off
rem Builds the F.L.A.M.E.S Windows app-image and exe installer.
rem Needs JDK 21. The exe step additionally needs WiX 3.x candle/light
rem on PATH (https://wixtoolset.org; JDK 21 jpackage cannot use WiX 4+);
rem without it, only the app-image builds.
setlocal
cd /d "%~dp0"
set VERSION=1.0.0
if not defined JAVA_HOME (
  if exist "C:\Users\Luikz\.jdks\jdk21\bin\java.exe" (
    set "JAVA_HOME=C:\Users\Luikz\.jdks\jdk21"
  )
)
if not exist "%JAVA_HOME%\bin\jpackage.exe" (
  echo ERROR: JDK 21 with jpackage not found. Set JAVA_HOME and retry.
  exit /b 1
)
call mvnw.cmd -B clean verify
if errorlevel 1 exit /b 1
call mvnw.cmd -B -DskipTests package
if errorlevel 1 exit /b 1
copy /y target\flames-%VERSION%.jar target\libs\ >nul
if errorlevel 1 exit /b 1
if exist target\installer rmdir /s /q target\installer
set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
"%JPACKAGE%" --type app-image --dest target\installer --name "F.L.A.M.E.S" ^
  --app-version %VERSION% --vendor "DuhItzAniket" --copyright "DuhItzAniket" ^
  --description "Modern revival of the classic FLAMES name game." ^
  --input target\libs --main-jar flames-%VERSION%.jar --main-class flames.Launcher ^
  --icon src\main\resources\assets\icon\flames.ico
if errorlevel 1 exit /b 1
where candle.exe >nul 2>&1
if errorlevel 1 (
  echo WiX not found - exe installer skipped. App image is at target\installer\F.L.A.M.E.S.
  exit /b 0
)
"%JPACKAGE%" --type exe --dest target\installer --name "F.L.A.M.E.S" ^
  --app-version %VERSION% --vendor "DuhItzAniket" --copyright "DuhItzAniket" ^
  --description "Modern revival of the classic FLAMES name game." ^
  --input target\libs --main-jar flames-%VERSION%.jar --main-class flames.Launcher ^
  --icon src\main\resources\assets\icon\flames.ico ^
  --win-menu --win-menu-group "F.L.A.M.E.S" --win-shortcut --win-dir-chooser --win-per-user-install
if errorlevel 1 exit /b 1
echo Done. Installers in target\installer.
