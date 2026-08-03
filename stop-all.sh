#!/usr/bin/env bash
# Stops services started by ./run-all.sh, using the PIDs it recorded.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$ROOT_DIR/.pids"

shopt -s nullglob
for pidfile in "$PID_DIR"/*.pid; do
  name=$(basename "$pidfile" .pid)
  pid=$(cat "$pidfile")
  if kill -0 "$pid" 2>/dev/null; then
    echo "Stopping $name (pid $pid)..."
    pkill -P "$pid" 2>/dev/null   # mvnw's child java process, if not exec'd
    kill "$pid" 2>/dev/null
  else
    echo "$name (pid $pid) already stopped."
  fi
  rm -f "$pidfile"
done

echo "Done."
