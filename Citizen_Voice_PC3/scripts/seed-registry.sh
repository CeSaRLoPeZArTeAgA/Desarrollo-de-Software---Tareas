#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/compile.sh"
COUNT="${1:-100000}"
KEY_SIZE="${2:-2048}"
WORKERS="${3:-8}"
java -cp build/classes pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli seed --count "$COUNT" --key-size "$KEY_SIZE" --workers "$WORKERS"
