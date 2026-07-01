from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

from pet_alerts.config import SettingsSingleton
from pet_alerts.main import create_app
from pet_alerts.repository import SQLiteRepository


@pytest.fixture()
def client(tmp_path):
    db_path = tmp_path / "test.db"
    upload_dir = tmp_path / "uploads"
    SettingsSingleton.reset_for_tests(db_path, upload_dir)
    repo = SQLiteRepository(db_path, seed_demo=False)
    app = create_app(repo)
    with TestClient(app) as test_client:
        yield test_client
