#!/bin/bash
PID=$(jps -l 2>/dev/null | grep 'server-2.0.0.jar' | awk '{print $1}')
if [ -n "$PID" ]; then
    kill $PID && echo "Stopped pid=$PID"
else
    echo "Not running."
fi
