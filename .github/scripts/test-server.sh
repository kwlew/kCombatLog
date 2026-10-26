#!/usr/bin/env bash
# Boots a real Paper server with the plugin in <SERVER_DIR>/plugins and verifies it enables cleanly.
# Usage: [SERVER_DIR=server] test-server.sh <minecraft-version>
set -euo pipefail

MC="${1:?usage: test-server.sh <minecraft-version>}"
SERVER_DIR="${SERVER_DIR:-server}"
START_TIMEOUT=300
STOP_TIMEOUT=60

mkdir -p "$SERVER_DIR"
cd "$SERVER_DIR"
mkdir -p plugins

if ! ls plugins/*.jar >/dev/null 2>&1; then
  echo "FAIL: no plugin jar found in $(pwd)/plugins"
  exit 1
fi

UA="kCombatLog-CI (github.com/kwlew/kCombatLog)"
echo "Resolving latest Paper build for $MC..."
URL="$(curl -fsSL -H "User-Agent: $UA" "https://fill.papermc.io/v3/projects/paper/versions/$MC/builds/latest" | jq -r '.downloads."server:default".url')"
if [ -z "$URL" ] || [ "$URL" = "null" ]; then
  echo "FAIL: could not resolve a Paper download URL for $MC"
  exit 1
fi
echo "Downloading $URL"
curl -fsSL -H "User-Agent: $UA" -o paper.jar "$URL"

echo "eula=true" > eula.txt
cat > server.properties <<'PROPS'
online-mode=false
server-port=25565
level-type=minecraft\:flat
generate-structures=false
spawn-protection=0
PROPS

: > console.log
rm -f stdin.fifo
mkfifo stdin.fifo
# Hold the FIFO open read-write so the server never sees EOF on stdin.
exec 3<>stdin.fifo

java -Xmx2G -jar paper.jar --nogui <&3 > >(tee console.log) 2>&1 &
SERVER_PID=$!

cleanup() {
  if kill -0 "$SERVER_PID" 2>/dev/null; then
    kill "$SERVER_PID" 2>/dev/null || true
  fi
  exec 3>&- || true
  rm -f stdin.fifo
}
trap cleanup EXIT

fail() {
  echo
  echo "FAIL ($MC): $1"
  echo "----- last 100 lines of console.log -----"
  tail -n 100 console.log || true
  echo "-----------------------------------------"
  exit 1
}

reached_done=0
for ((i = 0; i < START_TIMEOUT; i++)); do
  if grep -q 'Done (' console.log; then
    reached_done=1
    break
  fi
  if ! kill -0 "$SERVER_PID" 2>/dev/null; then
    break
  fi
  sleep 1
done

if [ "$reached_done" -eq 1 ]; then
  # Give the plugin a moment, then stop gracefully.
  sleep 2
  echo "stop" >&3
  for ((i = 0; i < STOP_TIMEOUT; i++)); do
    kill -0 "$SERVER_PID" 2>/dev/null || break
    sleep 1
  done
fi
if kill -0 "$SERVER_PID" 2>/dev/null; then
  echo "Server still running, killing it."
  kill "$SERVER_PID" 2>/dev/null || true
  sleep 3
  kill -9 "$SERVER_PID" 2>/dev/null || true
fi
wait "$SERVER_PID" 2>/dev/null || true
sleep 1 # let tee flush

[ "$reached_done" -eq 1 ] || fail "server never reached 'Done (' within ${START_TIMEOUT}s"

grep -q '\[kCombatLog\] Enabling kCombatLog' console.log \
  || fail "missing '[kCombatLog] Enabling kCombatLog' line (plugin did not enable)"
grep -q 'Error occurred while enabling kCombatLog' console.log \
  && fail "'Error occurred while enabling kCombatLog' found"
grep -q "Could not load 'plugins/kCombatLog" console.log \
  && fail "plugin failed to load ('Could not load plugins/kCombatLog...')"
grep -q 'UnsupportedClassVersionError' console.log \
  && fail "UnsupportedClassVersionError found"
if grep -q 'dev\.kwlew\.kCombatLog' console.log && grep -E 'dev\.kwlew\.kCombatLog\..*\(.*\.java:[0-9]+\)|^\s+at dev\.kwlew\.kCombatLog' console.log >/dev/null; then
  fail "kCombatLog stack frames found in log: $(grep -E 'at dev\.kwlew\.kCombatLog' console.log | head -n 3 | tr '\n' ' ')"
fi

echo
echo "PASS ($MC): kCombatLog enabled cleanly and server reached Done."
