@echo off
cd /d "%~dp0"
call mvn clean package -DskipTests
if errorlevel 1 (
    echo Build FAILED
    exit /b 1
)
echo Build done: target\server-2.0.0.jar
