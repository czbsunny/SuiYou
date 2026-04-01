@echo off

REM 检查Python3是否安装
python3 --version > nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Python3 未安装，请先安装Python3
    pause
    exit /b 1
)

REM 创建虚拟环境（如果不存在）
if not exist "venv" (
    echo 创建Python虚拟环境...
    python3 -m venv venv
    if %errorlevel% neq 0 (
        echo 错误: 创建虚拟环境失败
        pause
        exit /b 1
    )
)

REM 激活虚拟环境
echo 激活虚拟环境...
call venv\Scripts\activate
if %errorlevel% neq 0 (
    echo 错误: 激活虚拟环境失败
    pause
    exit /b 1
)

REM 升级pip
echo 升级pip...
pip install --upgrade pip
if %errorlevel% neq 0 (
    echo 警告: pip升级失败，继续尝试安装依赖
)

REM 安装依赖
echo 安装依赖...
pip install -r requirements.txt
if %errorlevel% neq 0 (
    echo 错误: 安装依赖失败
    pause
    exit /b 1
)

REM 启动服务
echo 启动数据服务(后台运行)...
REM 使用start命令在后台运行，并将标准输出和错误输出重定向到log文件
start "DataServer" /B cmd /c "python main.py > data_server.log 2>&1"
if %errorlevel% neq 0 (
    echo 错误: 启动服务失败
    pause
    exit /b 1
)
echo 服务已在后台启动，日志输出到 data_server.log 文件
echo 提示：可以使用 'tasklist | findstr python' 查看进程