#!/usr/bin/env bash
# Starts the financial-district monolith.
# Logs go to logs/financial-district.log, PID to .pids/financial-district.pid.
# Use ./stop.sh to stop it.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/.pids"
mkdir -p "$LOG_DIR" "$PID_DIR"

if [[ ! -f "$ROOT_DIR/.env" ]]; then
  echo "Missing .env file at $ROOT_DIR/.env — copy .env.example to .env and fill in real values." >&2
  exit 1
fi
set -o allexport
source "$ROOT_DIR/.env"
set +o allexport

echo "Starting financial-district..."
( cd "$ROOT_DIR" && exec ./mvnw -q spring-boot:run ) > "$LOG_DIR/financial-district.log" 2>&1 &
echo $! > "$PID_DIR/financial-district.pid"

wait_for_port() {
  local port=$1
  echo "Waiting for financial-district on port $port..."
  for _ in $(seq 1 60); do
    if (exec 3<>"/dev/tcp/127.0.0.1/$port") 2>/dev/null; then
      exec 3>&- 3<&-
      echo "financial-district is up."
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for financial-district on port $port" >&2
  exit 1
}

wait_for_port 8080

# spring-boot-devtools forces spring-boot:run to fork the app into a child
# JVM; once that fork happens the mvnw/Maven process we backgrounded above
# exits on its own, leaving the actual app running as an orphan under a
# different PID. Repoint the pid file at the real long-lived process now
# that it's confirmed up, so stop.sh can actually stop it.
real_pid=$(pgrep -f "$ROOT_DIR/target/classes" | head -1)
if [[ -n "$real_pid" ]]; then
  echo "$real_pid" > "$PID_DIR/financial-district.pid"
fi

echo "financial-district is starting. Log: $LOG_DIR/financial-district.log  PID: $PID_DIR/financial-district.pid"
echo "Run ./stop.sh to stop it."
