@echo off
setlocal enabledelayedexpansion

rem Lumora Maven Wrapper (ban rut gon): tu tai Maven ve local neu chua co, roi chay build.
rem Muon dung ban wrapper chinh thuc cua Apache, chay: mvn -N wrapper:wrapper -Dmaven=3.9.9

set "WRAPPER_DIR=%~dp0"
set "PROPS_FILE=%WRAPPER_DIR%.mvn\wrapper\maven-wrapper.properties"

for /f "usebackq tokens=1,* delims==" %%A in ("%PROPS_FILE%") do (
    if "%%A"=="distributionUrl" set "DIST_URL=%%B"
)

for %%F in ("%DIST_URL%") do set "ARCHIVE_NAME=%%~nxF"
set "DIR_NAME=%ARCHIVE_NAME:-bin.zip=%"
set "DISTS_DIR=%WRAPPER_DIR%.mvn\wrapper\dists"
set "MVN_BIN=%DISTS_DIR%\%DIR_NAME%\bin\mvn.cmd"

if not exist "%MVN_BIN%" (
    echo Dang tai Maven ve: %DIST_URL%
    if not exist "%DISTS_DIR%" mkdir "%DISTS_DIR%"
    powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%DISTS_DIR%\%ARCHIVE_NAME%'"
    powershell -NoProfile -Command "Expand-Archive -Path '%DISTS_DIR%\%ARCHIVE_NAME%' -DestinationPath '%DISTS_DIR%' -Force"
    del "%DISTS_DIR%\%ARCHIVE_NAME%"
)

"%MVN_BIN%" %*
