$ErrorActionPreference = "Stop"
$env:PYTHONPATH = "src"
uvicorn pet_alerts.main:app --reload --host 0.0.0.0 --port 8000
