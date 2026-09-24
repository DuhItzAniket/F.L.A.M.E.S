@echo off
rem Launches F.L.A.M.E.S for manual testing. Double-click or run from anywhere.
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME (
  if exist "C:\Users\Luikz\.jdks\jdk21\bin\java.exe" (
    set "JAVA_HOME=C:\Users\Luikz\.jdks\jdk21"
  )
)
call mvnw.cmd javafx:run
if errorlevel 1 (
  echo.
  echo FAILED to launch. You need JDK 21, plus internet on the first run.
  pause
)
