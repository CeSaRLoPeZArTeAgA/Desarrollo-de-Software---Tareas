from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from threading import Lock


@dataclass(frozen=True)
class AppSettings:
    """Configuración global del sistema.

    Patrón creacional usado: Singleton.
    La aplicación lee una única configuración centralizada para latencia,
    radio de alertas, base de datos y almacenamiento de imágenes.
    """

    app_name: str
    default_alert_radius_meters: int
    alert_latency_limit_seconds: float
    database_path: Path
    upload_dir: Path


class SettingsSingleton:
    _instance: AppSettings | None = None
    _lock = Lock()

    @classmethod
    def get(cls) -> AppSettings:
        with cls._lock:
            if cls._instance is None:
                cls._instance = AppSettings(
                    app_name=os.getenv("APP_NAME", "UNI Mascotas"),
                    default_alert_radius_meters=int(os.getenv("DEFAULT_ALERT_RADIUS_METERS", "1000")),
                    alert_latency_limit_seconds=float(os.getenv("ALERT_LATENCY_LIMIT_SECONDS", "5")),
                    database_path=Path(os.getenv("DATABASE_PATH", "data/mascotas_uni.db")),
                    upload_dir=Path(os.getenv("UPLOAD_DIR", "src/pet_alerts/uploads")),
                )
                cls._instance.database_path.parent.mkdir(parents=True, exist_ok=True)
                cls._instance.upload_dir.mkdir(parents=True, exist_ok=True)
            return cls._instance

    @classmethod
    def reset_for_tests(cls, database_path: Path, upload_dir: Path) -> None:
        with cls._lock:
            cls._instance = AppSettings(
                app_name="UNI Mascotas Test",
                default_alert_radius_meters=1000,
                alert_latency_limit_seconds=5,
                database_path=database_path,
                upload_dir=upload_dir,
            )
            database_path.parent.mkdir(parents=True, exist_ok=True)
            upload_dir.mkdir(parents=True, exist_ok=True)
