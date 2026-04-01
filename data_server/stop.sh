#!/bin/bash

# data_server 停止脚本 (Linux/Mac)
# 使用方法: chmod +x stop.sh && ./stop.sh

echo "正在停止数据服务..."

# 查找运行main.py的Python进程
APP_PID=""
echo "正在查找运行中的Python服务进程..."
APP_PID=$(ps aux | grep "python main.py" | grep -v grep | awk '{print $2}')

if [ -z "$APP_PID" ]; then
    echo "未找到运行中的数据服务进程。"
    exit 0
fi

# 如果找到多个进程，让用户选择
if [ $(echo "$APP_PID" | wc -w) -gt 1 ]; then
    echo "找到多个可能的数据服务进程:"
    ps aux | grep "python main.py" | grep -v grep | awk '{print $2, $11, $12}'
    echo
    read -p "请输入要停止的进程ID (默认全部停止): " SELECTED_PID
    
    if [ -n "$SELECTED_PID" ]; then
        APP_PID="$SELECTED_PID"
    fi
fi

# 停止进程
for PID in $APP_PID; do
    echo "正在停止进程 $PID..."
    
    # 首先尝试优雅停止 (SIGTERM)
    kill "$PID"
    
    # 等待进程终止，最多等待5秒
    echo "等待进程终止..."
    wait_count=0
    while ps -p "$PID" > /dev/null && [ "$wait_count" -lt 5 ]; do
        sleep 1
        wait_count=$((wait_count + 1))
    done
    
    # 如果进程仍在运行，强制终止 (SIGKILL)
    if ps -p "$PID" > /dev/null; then
        echo "进程未响应，尝试强制终止..."
        kill -9 "$PID"
    fi
    
    # 再次检查进程是否已终止
    if ps -p "$PID" > /dev/null; then
        echo "错误: 无法终止进程 $PID"
    else
        echo "进程 $PID 已成功终止。"
    fi
done

echo "数据服务停止完成!"