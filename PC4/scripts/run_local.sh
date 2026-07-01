#!/usr/bin/env bash
set -euo pipefail
python -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements-dev.txt
PYTHONPATH=src uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000
