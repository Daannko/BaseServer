@echo off
REM Double-click: start the stack with the frontend reachable from other devices on the network.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" -Lan %*
pause
