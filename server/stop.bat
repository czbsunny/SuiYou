@echo off
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Found PID %%a on port 8080
    taskkill /PID %%a /F
    goto :eof
)
echo Nothing on port 8080.
