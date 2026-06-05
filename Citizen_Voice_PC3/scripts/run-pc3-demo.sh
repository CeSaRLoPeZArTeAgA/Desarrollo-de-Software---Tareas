#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/compile.sh"
PROPOSAL_ID="${1:-1}"
SAMPLE_SIZE="${2:-25000}"
java -cp build/classes pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceCli run-demo --proposal-id "$PROPOSAL_ID" --sample-size "$SAMPLE_SIZE"
