#!/bin/bash

# Spring Boot 应用后台启动脚本 (Linux/Mac)
# 使用方法: chmod +x start.sh && ./start.sh
# 注意: 使用前请确保已运行 ./build.sh 编译项目

echo "正在后台启动 SuiYou 服务器应用..."

# 检查 Java 版本
check_java_version() {
    if ! command -v java &> /dev/null; then
        echo "错误: 未找到 Java。请安装 Java 17 或更高版本。"
        return 1
    fi

    # 获取 Java 主版本号
    java_version=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}')
    major_version=$(echo $java_version | awk -F. '{if ($1 >= 1) print ($1 == 1 ? $2 : $1); else print $1}')

    echo "当前 Java 版本: $java_version"

    if [ "$major_version" -lt 17 ]; then
        echo "错误: Java 版本 $java_version 低于要求的 Java 17。"
        return 1
    fi

    echo "Java 版本检查通过!"
    return 0
}

# 检查是否存在构建产物
check_build_artifact() {
    JAR_FILE=$(find target -name "*.jar" 2>/dev/null | head -n 1)

    if [ -z "$JAR_FILE" ]; then
        echo "错误: 未找到构建产物。请先运行 ./build.sh 编译项目。"
        return 1
    fi

    echo "找到构建产物: $JAR_FILE"
    return 0
}

# 检查应用是否已经在运行
check_running_instance() {
    if pgrep -f "java.*target.*jar" > /dev/null; then
        echo "警告: 检测到应用可能已经在运行。"
        read -p "是否继续启动新实例？(y/n): " continue
        if [ "$continue" != "y" ] && [ "$continue" != "Y" ]; then
            echo "已取消启动。"
            exit 0
        fi
    fi
}

# 检查 Java 版本
if ! check_java_version; then
    exit 1
fi

# 检查构建产物
if ! check_build_artifact; then
    exit 1
fi

# 检查是否已有实例在运行
check_running_instance

# 创建日志目录
mkdir -p logs

# 后台启动应用
echo "正在启动应用 (后台模式)..."
JAR_FILE=$(find target -name "*.jar" 2>/dev/null | head -n 1)

# 使用 nohup 实现后台运行，并重定向输出到日志文件
nohup java -jar "$JAR_FILE" > logs/app.log 2>&1 &

# 保存进程ID
APP_PID=$!
echo "$APP_PID" > app.pid

echo "应用已在后台启动，进程ID: $APP_PID"
echo "日志输出到: logs/app.log"
echo "停止应用: kill $APP_PID 或 ./stop.sh"
echo "查看日志: tail -f logs/app.log"

# 简单等待并检查应用是否正常启动
sleep 2
if ps -p "$APP_PID" > /dev/null; then
    echo "应用启动成功!"
else
    echo "警告: 应用可能启动失败，请检查日志: logs/app.log"
fi