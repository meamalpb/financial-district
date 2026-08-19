#!/usr/bin/env bash
# Stops the financial-district monolith started by ./run.sh.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$ROOT_DIR/.pids"
PID_FILE="$PID_DIR/financial-district.pid"

if [[ -f "$PID_FILE" ]]; then
  pid=$(cat "$PID_FILE")
  if kill -0 "$pid" 2>/dev/null; then
    echo "Stopping financial-district (pid $pid)..."
    pkill -P "$pid" 2>/dev/null   # mvnw's child java process, if not exec'd
    kill "$pid" 2>/dev/null
  else
    echo "financial-district (pid $pid) already stopped."
  fi
  rm -f "$PID_FILE"
else
  echo "No PID file found at $PID_FILE — nothing to stop."
fi

echo "Done."
