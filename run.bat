@echo off
REM Exit immediately if a command exits with a non-zero status
setlocal enabledelayedexpansion

call echo Removing all useless images
call docker images -f "dangling=true" -q | xargs docker rmi

REM Print commands to be executed
call echo Running Maven clean install...
call mvn clean install -Dmaven.test.skip=true

call echo Stopping and removing Docker containers...
call docker-compose down

call echo Building and starting Docker containers...
docker-compose up --build -d

