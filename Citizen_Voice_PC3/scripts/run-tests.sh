#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/compile.sh"
mkdir -p build/test-classes
find src/test/java -name "*.java" -print > build/test-sources.txt
if [[ ! -s build/test-sources.txt ]]; then
  echo "ERROR: No se encontraron pruebas en src/test/java" >&2
  exit 1
fi
javac -encoding UTF-8 -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp build/classes:build/test-classes pe.edu.uni.fc.cc.citizenvoice.TestRunner
