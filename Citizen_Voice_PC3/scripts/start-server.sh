#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/compile.sh"
PORT="${CITIZEN_VOICE_PORT:-8080}"
echo "Iniciando Citizen Voice PC3 en http://127.0.0.1:${PORT}"
java -cp build/classes pe.edu.uni.fc.cc.citizenvoice.app.CitizenVoiceServer
