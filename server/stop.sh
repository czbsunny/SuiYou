#!/bin/bash

# Spring Boot 应用停止脚本 (Linux/Mac)
# 使用方法: chmod +x stop.sh && ./stop.sh

echo "正在停止 SuiYou 服务器应用..."

# 尝试从 pid 文件获取进程ID
APP_PID=""
if [ -f app.pid ]; then
    APP_PID=$(cat app.pid)
    echo "从 app.pid 文件读取到进程ID: $APP_PID"

    # 检查进程是否存在
    if ! ps -p "$APP_PID" > /dev/null; then
        echo "警告: PID $APP_PID 不存在或已终止。"
        rm -f app.pid
        APP_PID=""
    fi
fi

# 如果没有从 pid 文件获取到有效 PID，则尝试查找进程
if [ -z "$APP_PID" ]; then
    echo "正在查找应用进程..."
    APP_PID=$(pgrep -f "java.*target.*jar")

    if [ -z "$APP_PID" ]; then
        echo "未找到运行中的应用进程。"
        exit 0
    fi

    # 如果找到多个进程，让用户选择
    if [ $(echo "$APP_PID" | wc -w) -gt 1 ]; then
        echo "找到多个可能的应用进程: $APP_PID"
        read -p "请输入要停止的进程ID (默认全部停止): " SELECTED_PID

        if [ -n "$SELECTED_PID" ]; then
            APP_PID="$SELECTED_PID"
        fi
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

# 清理 pid 文件
if [ -f app.pid ]; then
    rm -f app.pid
    echo "已删除 app.pid 文件。"
fi

echo "应用停止完成!"