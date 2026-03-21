#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
STATE_DIR="${CI_DEV_SERVER_WORK_DIR:-$ROOT_DIR/.ci/dev-server}"
PID_FILE="${CI_DEV_SERVER_PID_FILE:-$STATE_DIR/server.pid}"

if [[ ! -f "$PID_FILE" ]]; then
  echo "No dev server PID file found"
  exit 0
fi

pid="$(cat "$PID_FILE")"
if kill -0 "$pid" >/dev/null 2>&1; then
  kill "$pid" >/dev/null 2>&1 || true
  for _ in {1..20}; do
    if ! kill -0 "$pid" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done
  if kill -0 "$pid" >/dev/null 2>&1; then
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi
  echo "Stopped dev server PID $pid"
else
  echo "Dev server PID $pid is not running"
fi

rm -f "$PID_FILE"
