#!/usr/bin/env bash
# Blocks `git commit` until the ContactManagerApp definition of done passes
# (see CLAUDE.md). Wired via .claude/settings.local.json PreToolUse hook.
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT" || exit 2

ensure_java17() {
  if mvn -v 2>/dev/null | grep -q "Java version: 17"; then
    return 0
  fi
  local candidate
  candidate=$(ls -d "/c/Program Files"/OpenLogic/jdk-17* "/c/Program Files"/Eclipse\ Adoptium/jdk-17* "/c/Program Files"/Java/jdk-17* 2>/dev/null | head -n1)
  if [ -n "$candidate" ]; then
    export JAVA_HOME="$candidate"
    export PATH="$candidate/bin:$PATH"
  fi
}

LOG="$(mktemp)"

run_step() {
  local desc="$1"; shift
  echo "==> $desc" >>"$LOG"
  if ! "$@" >>"$LOG" 2>&1; then
    echo "" >&2
    echo "Definition-of-done gate FAILED at: $desc" >&2
    echo "---- last 50 lines of output ----" >&2
    tail -n 50 "$LOG" >&2
    rm -f "$LOG"
    exit 2
  fi
}

ensure_java17

cd "$ROOT/backend" || exit 2
run_step "backend: mvn clean test" mvn clean test
run_step "backend: mvn clean package" mvn clean package

cd "$ROOT/frontend" || exit 2
if [ ! -d node_modules ]; then
  run_step "frontend: npm install" npm install
fi
run_step "frontend: npm run lint" npm run lint
run_step "frontend: npm run typecheck" npm run typecheck
run_step "frontend: npm run build" npm run build

rm -f "$LOG"
exit 0
