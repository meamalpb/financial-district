#!/usr/bin/env bash
# Starts all microservices in dependency order:
#   discovery-server -> config-server -> (price-service, strategy-service)
# Logs go to logs/<service>.log, PIDs to .pids/<service>.pid.
# Use ./stop-all.sh to stop everything.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/.pids"
mkdir -p "$LOG_DIR" "$PID_DIR"

wait_for_port() {
  local port=$1 name=$2
  echo "Waiting for $name on port $port..."
  for _ in $(seq 1 60); do
    if (exec 3<>"/dev/tcp/127.0.0.1/$port") 2>/dev/null; then
      exec 3>&- 3<&-
      echo "$name is up."
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for $name on port $port" >&2
  exit 1
}

start_service() {
  local name=$1 dir=$2
  echo "Starting $name..."
  ( cd "$ROOT_DIR/$dir" && exec ./mvnw -q spring-boot:run ) > "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$PID_DIR/$name.pid"
}

start_service discovery-server discovery-server
wait_for_port 8761 discovery-server

start_service config-server config-server
wait_for_port 8888 config-server

start_service price-service price-service
start_service strategy-service strategy-service

echo "All services starting. Logs: $LOG_DIR  PIDs: $PID_DIR"
echo "Run ./stop-all.sh to stop everything."
