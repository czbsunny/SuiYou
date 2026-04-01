#!/bin/bash

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "错误: Python3 未安装，请先安装Python3"
    exit 1
fi

# 创建虚拟环境（如果不存在）
if [ ! -d "venv" ]; then
    echo "创建Python虚拟环境..."
    python3 -m venv venv
    if [ $? -ne 0 ]; then
        echo "错误: 创建虚拟环境失败"
        exit 1
    fi
fi

# 激活虚拟环境
echo "激活虚拟环境..."
source venv/bin/activate

# 升级pip
echo "升级pip..."
pip install --upgrade pip

# 安装依赖
echo "安装依赖..."
pip install -r requirements.txt
if [ $? -ne 0 ]; then
    echo "错误: 安装依赖失败"
    exit 1
fi

# 启动服务
echo "启动数据服务(后台运行)..."
# 使用nohup在后台运行，并将标准输出和错误输出重定向到log文件
nohup python main.py > data_server.log 2>&1 &
echo "服务已在后台启动，日志输出到 data_server.log 文件"
# 显示进程ID，方便后续管理
echo "进程ID: $!"