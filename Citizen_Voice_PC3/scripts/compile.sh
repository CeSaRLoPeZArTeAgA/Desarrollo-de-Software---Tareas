#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"
mkdir -p build/classes
find src/main/java -name "*.java" -print > build/sources.txt
if [[ ! -s build/sources.txt ]]; then
  echo "ERROR: No se encontraron archivos Java en src/main/java" >&2
  exit 1
fi
javac -encoding UTF-8 -d build/classes @build/sources.txt
echo "Compilacion completada correctamente en build/classes"
