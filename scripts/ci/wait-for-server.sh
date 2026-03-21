#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <health-url>"
  exit 1
fi

URL="$1"
TIMEOUT_SECONDS="${CI_DEV_SERVER_TIMEOUT_SECONDS:-120}"
LOG_FILE="${CI_DEV_SERVER_LOG_FILE:-}"

for ((attempt = 1; attempt <= TIMEOUT_SECONDS; attempt++)); do
  if curl --silent --show-error --fail "$URL" >/dev/null; then
    echo "Server is ready: $URL"
    exit 0
  fi
  sleep 1
done

echo "Timed out waiting for server readiness: $URL"
if [[ -n "$LOG_FILE" && -f "$LOG_FILE" ]]; then
  echo "Last server log lines:"
  tail -n 200 "$LOG_FILE"
fi
exit 1
