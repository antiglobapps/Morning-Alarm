#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
STATE_DIR="${CI_DEV_SERVER_WORK_DIR:-$ROOT_DIR/.ci/dev-server}"
PORT="${CI_DEV_SERVER_PORT:-8080}"
DATA_DIR="${CI_DEV_SERVER_DATA_DIR:-$STATE_DIR/data}"
LOG_FILE="${CI_DEV_SERVER_LOG_FILE:-$STATE_DIR/server.log}"
PID_FILE="${CI_DEV_SERVER_PID_FILE:-$STATE_DIR/server.pid}"

mkdir -p "$STATE_DIR" "$DATA_DIR" "$(dirname "$LOG_FILE")" "$(dirname "$PID_FILE")"

if [[ -f "$PID_FILE" ]]; then
  existing_pid="$(cat "$PID_FILE")"
  if kill -0 "$existing_pid" >/dev/null 2>&1; then
    echo "Dev server is already running with PID $existing_pid"
    exit 1
  fi
  rm -f "$PID_FILE"
fi

export SERVER_DEV_MODE="${SERVER_DEV_MODE:-true}"
export SERVER_HOST="${SERVER_HOST:-127.0.0.1}"
export SERVER_PORT="$PORT"
export SERVER_PUBLIC_URL="${SERVER_PUBLIC_URL:-http://127.0.0.1:$PORT}"
export SERVER_MEDIA_PUBLIC_BASE_URL="${SERVER_MEDIA_PUBLIC_BASE_URL:-http://127.0.0.1:$PORT}"
export SERVER_ADMIN_EMAILS="${SERVER_ADMIN_EMAILS:-admin@example.com}"
export SERVER_ADMIN_BOOTSTRAP_SECRET="${SERVER_ADMIN_BOOTSTRAP_SECRET:-bootstrap-dev-secret}"
export SERVER_ADMIN_ACCESS_SECRET="${SERVER_ADMIN_ACCESS_SECRET:-admin-dev-secret}"
export SERVER_JWT_SECRET="${SERVER_JWT_SECRET:-ci-dev-jwt-secret}"
export SERVER_DB_URL="${SERVER_DB_URL:-jdbc:h2:file:${DATA_DIR}/ci-dev-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH}"

GRADLE_CMD="${CI_GRADLE_CMD:-}"
if [[ -z "$GRADLE_CMD" ]]; then
  if [[ -x "$ROOT_DIR/.gradle-local/gradle-8.14/bin/gradle" ]]; then
    GRADLE_CMD="$ROOT_DIR/.gradle-local/gradle-8.14/bin/gradle"
  else
    GRADLE_CMD="$ROOT_DIR/gradlew"
  fi
fi

cd "$ROOT_DIR"
"$GRADLE_CMD" :server:installDist

SERVER_BIN="$ROOT_DIR/server/build/install/server/bin/server"
if [[ ! -x "$SERVER_BIN" ]]; then
  echo "Server binary was not generated: $SERVER_BIN"
  exit 1
fi

nohup "$SERVER_BIN" >"$LOG_FILE" 2>&1 &
echo "$!" > "$PID_FILE"
echo "Started dev server with PID $(cat "$PID_FILE")"
