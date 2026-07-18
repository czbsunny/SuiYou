@echo off
chcp 65001 >nul
cd /d "%~dp0"
set JAR=target\server-2.0.0.jar
if not exist %JAR% (
    echo JAR not found. Run build.bat first.
    exit /b 1
)
start "suiyou-server" java -Dfile.encoding=UTF-8 -jar %JAR%
echo Started.
