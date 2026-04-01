@echo off

REM data_server 停止脚本 (Windows)
REM 使用方法: 双击 stop.bat 或在命令提示符中运行

echo 正在停止数据服务...

REM 查找运行main.py的Python进程
setlocal enabledelayedexpansion
set "PID_LIST="

REM 获取所有Python进程并筛选出运行main.py的进程
echo 正在查找运行中的Python服务进程...
for /f "tokens=2,5" %%a in ('tasklist /fi "imagename eq python.exe" /fo list /v') do (
    if "%%a"=="PID:" (
        set "PID=%%b"
    ) else if "%%a"=="命令行:" (
        if "%%b"=="" (
            for /f "tokens=1*" %%c in ("%%b") do (
                if "%%c"=="python" (
                    for /f "tokens=2*" %%d in ("%%d") do (
                        if "%%d"=="main.py" (
                            set "PID_LIST=!PID_LIST! !PID!"
                            echo 找到进程: PID=!PID!, 命令行=%%c %%d
                        )
                    )
                )
            )
        )
    )
)

REM 如果没有找到进程，直接退出
if "%PID_LIST%"=="" (
    echo 未找到运行中的数据服务进程。
    pause
    exit /b 0
)

REM 询问用户是否停止所有找到的进程
echo.
echo 找到以下进程:
for %%p in (%PID_LIST%) do (
    tasklist /fi "PID eq %%p" /fo list | findstr /i "PID 映像名称 用户名"
    echo.
)

echo 是否停止所有找到的进程？(Y/N，默认Y):
set /p "CHOICE="
if /i "%CHOICE%"=="" set "CHOICE=Y"
if /i not "%CHOICE%"=="Y" (
    echo 已取消停止操作。
    pause
    exit /b 0
)

REM 停止进程
for %%p in (%PID_LIST%) do (
    echo 正在停止进程 %%p...
    taskkill /pid %%p /t /f
    if !errorlevel! equ 0 (
        echo 进程 %%p 已成功终止。
    ) else (
        echo 错误: 无法终止进程 %%p
    )
)

echo.
echo 数据服务停止完成!
pause