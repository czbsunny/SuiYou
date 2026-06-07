#!/bin/bash
set -e
cd "$(dirname "$0")"
mvn clean package -DskipTests
echo "Build done: target/server-2.0.0.jar"
