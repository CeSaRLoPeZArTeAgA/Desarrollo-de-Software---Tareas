from __future__ import annotations

import os
from pathlib import Path

from pet_alerts.repository import SQLiteRepository


def main() -> None:
    db_path = Path(os.getenv("DATABASE_PATH", "data/mascotas_uni.db"))
    repo = SQLiteRepository(db_path, seed_demo=True)
    print(f"Base de datos inicializada: {repo.db_path}")
    print(f"Reportes: {len(repo.list_reports_filtered())}")
    print(f"Cuidadores: {len(repo.list_caregivers())}")
    print(f"Adopciones: {len(repo.list_adoption_catalog())}")
    print(f"Criaderos certificados: {len(repo.list_certified_breeders())}")


if __name__ == "__main__":
    main()
