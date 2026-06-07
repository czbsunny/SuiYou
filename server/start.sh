#!/bin/bash
cd "$(dirname "$0")"
JAR="target/server-2.0.0.jar"
if [ ! -f "$JAR" ]; then
    echo "JAR not found. Run ./build.sh first."
    exit 1
fi
nohup java -jar "$JAR" > app.log 2>&1 &
echo "Started, pid=$!"
