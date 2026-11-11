@echo off
REM Double-click launcher for start-all.ps1 (bypasses PowerShell execution policy).
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" %*
pause
