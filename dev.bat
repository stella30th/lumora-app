@echo off
setlocal
title Lumora - Start Dev Servers
set "ROOT=%~dp0"

echo ============================================
echo   Lumora - starting backend + frontend
echo ============================================
echo.

set "PG_RUNNING="
for /f %%i in ('docker ps --filter "name=lumora-postgres" --filter "status=running" -q 2^>nul') do set "PG_RUNNING=%%i"
if "%PG_RUNNING%"=="" (
    echo [WARNING] Postgres container "lumora-postgres" does not look like it is running.
    echo           If the backend fails to start with a DB connection error, run:
    echo             cd backend ^&^& docker-compose up -d
    echo.
)

echo Starting backend  (new window)...
start "Lumora Backend" cmd /k "cd /d %ROOT%backend && mvnw.cmd spring-boot:run"

echo Starting frontend (new window)...
start "Lumora Frontend" cmd /k "cd /d %ROOT%frontend && npm run dev"

echo.
echo Both windows are launching in separate consoles.
echo Backend needs about 10-20 seconds to boot; once you see
echo "Started LumoraApplication" in the backend window, the
echo frontend at http://localhost:3000 will be able to reach it.
echo.
pause
