#!/bin/bash

# Spring Boot 应用构建脚本 (Linux/Mac)
# 使用方法: chmod +x build.sh && ./build.sh

echo "正在构建 SuiYou 服务器应用..."

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

# 检查并尝试安装 Java 17 (仅支持 Ubuntu)
install_java_17() {
    if [ -f /etc/os-release ] && grep -q "Ubuntu" /etc/os-release; then
        echo "检测到 Ubuntu 系统，正在尝试自动安装 Java 17..."

        # 更新软件包列表
        echo "更新软件包列表..."
        sudo apt-get update

        # 安装 OpenJDK 17
        echo "安装 OpenJDK 17..."
        sudo apt-get install -y openjdk-17-jdk

        # 验证安装
        if ! command -v java &> /dev/null; then
            echo "错误: Java 17 安装失败。请手动安装 Java 17 后重试。"
            return 1
        fi

        echo "Java 17 安装成功！"
        return 0
    else
        echo "提示: 当前系统不是 Ubuntu，无法自动安装 Java 17。"
        echo "请手动安装 Java 17 或更高版本，并确保已添加到系统环境变量中。"
        return 1
    fi
}

# 检查是否安装了 Maven
check_maven_installation() {
    if ! command -v mvn &> /dev/null; then
        echo "未找到 Maven，正在检查系统类型..."

        # 检查是否是 Ubuntu 系统
        if [ -f /etc/os-release ] && grep -q "Ubuntu" /etc/os-release; then
            echo "检测到 Ubuntu 系统，正在尝试自动安装 Maven..."

            # 更新软件包列表
            echo "更新软件包列表..."
            sudo apt-get update

            # 安装 Maven 3.6 版本
            echo "安装 Maven 3.6 版本..."
            # 首先确保 universe 仓库已启用
            sudo apt-get install -y software-properties-common
            sudo add-apt-repository universe
            sudo apt-get update
            # 尝试安装 Maven 3.6 版本
            sudo apt-get install -y maven=3.6.*

            # 如果直接安装特定版本失败，则尝试手动安装
            if ! command -v mvn &> /dev/null || ! mvn -v | grep -q "3.6"; then
                echo "直接安装特定版本失败，尝试手动下载并安装 Maven 3.6.3..."

                # 创建安装目录
                sudo mkdir -p /opt/maven

                # 下载 Maven 3.6.3
                wget -q https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -O /tmp/maven.tar.gz

                # 解压到安装目录
                sudo tar -xzf /tmp/maven.tar.gz -C /opt/maven --strip-components=1

                # 设置环境变量
                echo '# Maven environment variables' | sudo tee -a /etc/profile.d/maven.sh
                echo 'export M2_HOME=/opt/maven' | sudo tee -a /etc/profile.d/maven.sh
                echo 'export PATH=${M2_HOME}/bin:${PATH}' | sudo tee -a /etc/profile.d/maven.sh
                sudo chmod +x /etc/profile.d/maven.sh

                # 立即加载环境变量
                source /etc/profile.d/maven.sh
                rm /tmp/maven.tar.gz

                echo "手动安装 Maven 3.6.3 完成。"
            fi

            # 检查安装是否成功
            if ! command -v mvn &> /dev/null; then
                echo "错误: Maven 安装失败。请手动安装 Maven 后重试。"
                return 1
            fi

            echo "Maven 安装成功！"
            return 0
        else
            echo "错误: 未找到 Maven，且当前系统不是 Ubuntu。请确保 Maven 已安装并且已添加到系统环境变量中。"
            return 1
        fi
    fi
    return 0
}

# 首先检查 Java 版本
if ! check_java_version; then
    read -p "是否尝试自动安装 Java 17？(y/n): " install_java
    if [ "$install_java" = "y" ] || [ "$install_java" = "Y" ]; then
        if ! install_java_17; then
            exit 1
        fi
        # 重新检查 Java 版本
        if ! check_java_version; then
            exit 1
        fi
    else
        exit 1
    fi
fi

# 检查 Maven 安装
if ! check_maven_installation; then
    exit 1
fi

# 显示 Maven 版本信息
echo "Maven 版本信息:"
mvn -v
echo

# 编译项目
echo "正在编译项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误: 项目编译失败，请检查日志信息。"
    exit 1
fi

echo "项目编译成功！"
echo "构建产物位于 target/ 目录下。"
echo "可以使用 ./start.sh 启动应用。"