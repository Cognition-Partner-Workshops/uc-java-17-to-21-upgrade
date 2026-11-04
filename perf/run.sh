#!/usr/bin/env bash
# Usage: perf/run.sh <conduit|loan> [target-url]   (extra latency.py flags via PERF_ARGS)
set -euo pipefail
APP="${1:?usage: perf/run.sh <conduit|loan> [target]}"
TARGET="${2:-}"
PERF_ARGS="${PERF_ARGS:-}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PY="${PYTHON:-python3}"

if [[ -n "$TARGET" ]]; then
  exec "$PY" "$ROOT/perf/latency.py" --app "$APP" --target "$TARGET" $PERF_ARGS
fi

WORK="$(mktemp -d)"
PORT="$("$PY" -c 'import socket; s=socket.socket(); s.bind(("",0)); print(s.getsockname()[1])')"
case "$APP" in
  conduit)
    JAR="$(ls "$ROOT"/conduit-api/build/libs/*.jar 2>/dev/null | grep -v plain || true)"
    if [[ -z "$JAR" ]]; then (cd "$ROOT/conduit-api" && ./gradlew --no-daemon -q bootJar); JAR="$(ls "$ROOT"/conduit-api/build/libs/*.jar | grep -v plain)"; fi
    ARGS=(--spring.datasource.url="jdbc:sqlite:$WORK/conduit.db")
    PROBE="/tags" ;;
  loan)
    JAR="$(ls "$ROOT"/loan-service/target/loan-service-*.jar 2>/dev/null | grep -v original || true)"
    if [[ -z "$JAR" ]]; then (cd "$ROOT/loan-service" && ./mvnw -B -q -DskipTests package); JAR="$(ls "$ROOT"/loan-service/target/loan-service-*.jar | grep -v original)"; fi
    ARGS=()
    PROBE="/api/loans" ;;
  *) echo "unknown app $APP" >&2; exit 2 ;;
esac

(cd "$WORK" && exec java -jar "$JAR" --server.port="$PORT" "${ARGS[@]}" > "$WORK/app.log" 2>&1) &
APP_PID=$!
trap 'kill $APP_PID 2>/dev/null || true; wait $APP_PID 2>/dev/null || true; rm -rf "$WORK"' EXIT
for _ in $(seq 1 90); do
  curl -sf "http://localhost:$PORT$PROBE" > /dev/null && break
  kill -0 $APP_PID 2>/dev/null || { cat "$WORK/app.log"; exit 1; }
  sleep 1
done
"$PY" "$ROOT/perf/latency.py" --app "$APP" --target "http://localhost:$PORT" $PERF_ARGS
