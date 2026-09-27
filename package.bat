@echo off
rem Builds the F.L.A.M.E.S Windows app-image and exe installer.
rem Needs JDK 21. The exe step additionally needs WiX 3.x candle/light
rem on PATH (https://wixtoolset.org); without it, only the app-image builds.
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME (
  if exist "C:\Users\Luikz\.jdks\jdk21\bin\java.exe" (
    set "JAVA_HOME=C:\Users\Luikz\.jdks\jdk21"
  )
)
call mvnw.cmd -B -DskipTests package
if errorlevel 1 exit /b 1
copy /y target\flames-1.0.0.jar target\libs\ >nul
if exist target\installer rmdir /s /q target\installer
if exist target\installer-debug rmdir /s /q target\installer-debug
set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
"%JPACKAGE%" --type app-image --dest target\installer --name "F.L.A.M.E.S" ^
  --app-version 1.0.0 --vendor "DuhItzAniket" --copyright "DuhItzAniket" ^
  --description "Modern revival of the classic FLAMES name game." ^
  --input target\libs --main-jar flames-1.0.0.jar --main-class flames.Launcher ^
  --icon src\main\resources\assets\icon\flames.ico
if errorlevel 1 exit /b 1
where candle.exe >nul 2>&1
if errorlevel 1 (
  echo WiX not found - exe installer skipped. App image is at target\installer\F.L.A.M.E.S.
  exit /b 0
)
"%JPACKAGE%" --type exe --dest target\installer --name "F.L.A.M.E.S" ^
  --app-version 1.0.0 --vendor "DuhItzAniket" --copyright "DuhItzAniket" ^
  --description "Modern revival of the classic FLAMES name game." ^
  --input target\libs --main-jar flames-1.0.0.jar --main-class flames.Launcher ^
  --icon src\main\resources\assets\icon\flames.ico ^
  --win-menu --win-menu-group "F.L.A.M.E.S" --win-shortcut --win-dir-chooser --win-per-user-install
