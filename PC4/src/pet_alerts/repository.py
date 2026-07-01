from __future__ import annotations

import sqlite3
from contextlib import contextmanager
from pathlib import Path
from typing import Iterator

from .models import (
    Caregiver,
    CaregiverRole,
    CaregiverStatus,
    Location,
    Owner,
    Pet,
    Report,
    ReportStatus,
    ReportType,
    SearchResult,
)


class SQLiteRepository:
    """Repositorio SQLite simple y portable para despliegue local o Docker."""

    def __init__(self, db_path: Path, seed_demo: bool = True):
        self.db_path = db_path
        self.seed_demo = seed_demo
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self.initialize()

    @contextmanager
    def connect(self) -> Iterator[sqlite3.Connection]:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
            conn.commit()
        finally:
            conn.close()

    def initialize(self) -> None:
        with self.connect() as conn:
            conn.executescript(
                """
                CREATE TABLE IF NOT EXISTS reports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    report_type TEXT NOT NULL,
                    pet_name TEXT NOT NULL,
                    species TEXT NOT NULL,
                    breed TEXT NOT NULL,
                    photo_path TEXT,
                    description TEXT NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    owner_name TEXT,
                    owner_email TEXT,
                    owner_phone TEXT,
                    reporter_alias TEXT,
                    status TEXT NOT NULL,
                    radius_meters INTEGER NOT NULL,
                    created_at TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS alert_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    report_id INTEGER NOT NULL,
                    recipients_count INTEGER NOT NULL,
                    channels TEXT NOT NULL,
                    latency_seconds REAL NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(report_id) REFERENCES reports(id)
                );

                CREATE TABLE IF NOT EXISTS caregivers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    dni TEXT NOT NULL UNIQUE,
                    role TEXT NOT NULL,
                    species_accepted TEXT NOT NULL,
                    size_accepted TEXT NOT NULL,
                    medication INTEGER NOT NULL,
                    accepts_lost_pet_alerts INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    average_rating REAL NOT NULL DEFAULT 0
                );

                CREATE TABLE IF NOT EXISTS adoption_catalog (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    species TEXT NOT NULL,
                    breed TEXT NOT NULL,
                    description TEXT NOT NULL,
                    location TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS breeder_catalog (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    species TEXT NOT NULL,
                    breed TEXT NOT NULL,
                    description TEXT NOT NULL,
                    location TEXT NOT NULL,
                    certified INTEGER NOT NULL
                );
                """
            )
            self._seed_catalogs(conn)
            if self.seed_demo:
                self._seed_demo_data(conn)

    def _seed_catalogs(self, conn: sqlite3.Connection) -> None:
        adoption_count = conn.execute("SELECT COUNT(*) FROM adoption_catalog").fetchone()[0]
        breeder_count = conn.execute("SELECT COUNT(*) FROM breeder_catalog").fetchone()[0]
        if adoption_count == 0:
            conn.executemany(
                "INSERT INTO adoption_catalog(title, species, breed, description, location) VALUES (?, ?, ?, ?, ?)",
                [
                    ("ONG Patitas UNI - Max", "perro", "mestizo", "Perro adulto sociable en adopción responsable.", "Lima Norte"),
                    ("Refugio San Marcos - Luna", "gato", "mestizo", "Gata joven esterilizada, apta para departamento.", "Cercado de Lima"),
                    ("Casa Temporal Animalista - Rocky", "perro", "labrador", "Macho joven con vacunas completas.", "Rímac"),
                ],
            )
        if breeder_count == 0:
            conn.executemany(
                "INSERT INTO breeder_catalog(title, species, breed, description, location, certified) VALUES (?, ?, ?, ?, ?, ?)",
                [
                    ("Criadero Canino Certificado Lima", "perro", "labrador", "Registro comercial vigente y control veterinario.", "La Molina", 1),
                    ("Centro Felino Certificado Perú", "gato", "siames", "Criadero con licencia municipal y trazabilidad.", "Miraflores", 1),
                    ("Criadero Andino Certificado", "perro", "golden retriever", "Licencia municipal activa y trazabilidad veterinaria completa.", "Surco", 1),
                    ("Anuncio no certificado", "perro", "mestizo", "Registro pendiente; no debe aparecer en resultados de venta.", "Lima", 0),
                ],
            )

    def _seed_demo_data(self, conn: sqlite3.Connection) -> None:
        report_count = conn.execute("SELECT COUNT(*) FROM reports").fetchone()[0]
        caregiver_count = conn.execute("SELECT COUNT(*) FROM caregivers").fetchone()[0]
        if report_count == 0:
            conn.executemany(
                """
                INSERT INTO reports(
                    report_type, pet_name, species, breed, photo_path, description,
                    latitude, longitude, owner_name, owner_email, owner_phone,
                    reporter_alias, status, radius_meters, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """,
                [
                    (ReportType.LOST_PET.value, "Max", "perro", "labrador", None, "Collar rojo. Última vez visto cerca de la UNI.", -12.0240, -77.0490, "César López", "dueno@example.com", "999888777", None, ReportStatus.ACTIVE.value, 1000),
                    (ReportType.LOST_PET.value, "Luna", "gato", "mestizo", None, "Gata gris con mancha blanca en el pecho.", -12.0464, -77.0428, "María Salas", "maria@example.com", "955222111", None, ReportStatus.ACTIVE.value, 1500),
                    (ReportType.SIGHTING.value, "Mascota avistada", "perro", "mestizo", None, "Perro pequeño avistado cerca de una tienda, parece desorientado.", -12.0302, -77.0501, None, None, None, "Vecino anónimo", ReportStatus.VERIFYING.value, 1000),
                    (ReportType.SIGHTING.value, "Mascota avistada", "gato", "siames", None, "Gato siamés visto en parque cercano.", -12.0440, -77.0310, None, None, None, "Ciudadano anónimo", ReportStatus.VERIFYING.value, 1000),
                ],
            )
        if caregiver_count == 0:
            conn.executemany(
                """
                INSERT INTO caregivers(
                    full_name, dni, role, species_accepted, size_accepted, medication,
                    accepts_lost_pet_alerts, status, average_rating
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                [
                    ("Ana Torres", "12345678", CaregiverRole.PROFESSIONAL.value, "perros, gatos", "pequeño, mediano", 1, 1, CaregiverStatus.VERIFIED.value, 4.8),
                    ("Luis Ramírez", "87654321", CaregiverRole.SOLIDARY.value, "perros", "mediano, grande", 0, 1, CaregiverStatus.VERIFIED.value, 4.5),
                    ("Claudia Vargas", "11223344", CaregiverRole.SPECIALIZED.value, "gatos", "pequeño", 1, 0, CaregiverStatus.VERIFIED.value, 4.9),
                ],
            )

    def list_adoption_catalog(self) -> list[dict]:
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM adoption_catalog ORDER BY id ASC").fetchall()
            return [dict(row) for row in rows]

    def list_certified_breeders(self) -> list[dict]:
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM breeder_catalog WHERE certified = 1 ORDER BY id ASC").fetchall()
            return [dict(row) for row in rows]

    def list_reports_filtered(self, report_type: str | None = None, species: str | None = None, limit: int = 60) -> list[Report]:
        query = "SELECT * FROM reports WHERE 1 = 1"
        params: list[str | int] = []
        if report_type and report_type != "all":
            query += " AND report_type = ?"
            params.append(report_type)
        if species:
            query += " AND lower(species) = ?"
            params.append(species.lower())
        query += " ORDER BY id DESC LIMIT ?"
        params.append(limit)
        with self.connect() as conn:
            rows = conn.execute(query, params).fetchall()
            return [self._row_to_report(row) for row in rows]

    def save_report(self, report: Report) -> Report:
        with self.connect() as conn:
            cur = conn.execute(
                """
                INSERT INTO reports(
                    report_type, pet_name, species, breed, photo_path, description,
                    latitude, longitude, owner_name, owner_email, owner_phone,
                    reporter_alias, status, radius_meters, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    report.report_type.value,
                    report.pet.name,
                    report.pet.species.lower(),
                    report.pet.breed.lower(),
                    report.pet.photo_path,
                    report.description,
                    report.location.latitude,
                    report.location.longitude,
                    report.owner.full_name if report.owner else None,
                    report.owner.email if report.owner else None,
                    report.owner.phone if report.owner else None,
                    report.reporter_alias,
                    report.status.value,
                    report.radius_meters,
                    report.created_at.isoformat(),
                ),
            )
            report.id = int(cur.lastrowid)
            return report

    def get_report(self, report_id: int) -> Report | None:
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM reports WHERE id = ?", (report_id,)).fetchone()
            return self._row_to_report(row) if row else None

    def list_active_lost_reports(self) -> list[Report]:
        with self.connect() as conn:
            rows = conn.execute(
                "SELECT * FROM reports WHERE report_type = ? AND status = ? ORDER BY id DESC",
                (ReportType.LOST_PET.value, ReportStatus.ACTIVE.value),
            ).fetchall()
            return [self._row_to_report(row) for row in rows]

    def list_recent_reports(self, limit: int = 10) -> list[Report]:
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM reports ORDER BY id DESC LIMIT ?", (limit,)).fetchall()
            return [self._row_to_report(row) for row in rows]

    def save_alert_event(self, report_id: int, recipients_count: int, channels: list[str], latency_seconds: float) -> None:
        with self.connect() as conn:
            conn.execute(
                "INSERT INTO alert_events(report_id, recipients_count, channels, latency_seconds) VALUES (?, ?, ?, ?)",
                (report_id, recipients_count, ",".join(channels), latency_seconds),
            )

    def save_caregiver(self, caregiver: Caregiver) -> Caregiver:
        try:
            with self.connect() as conn:
                cur = conn.execute(
                    """
                    INSERT INTO caregivers(
                        full_name, dni, role, species_accepted, size_accepted, medication,
                        accepts_lost_pet_alerts, status, average_rating
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    (
                        caregiver.full_name,
                        caregiver.dni,
                        caregiver.role.value,
                        caregiver.species_accepted,
                        caregiver.size_accepted,
                        int(caregiver.medication),
                        int(caregiver.accepts_lost_pet_alerts),
                        caregiver.status.value,
                        caregiver.average_rating,
                    ),
                )
                caregiver.id = int(cur.lastrowid)
                return caregiver
        except sqlite3.IntegrityError as exc:
            raise ValueError("Ya existe un cuidador registrado con ese DNI.") from exc

    def update_caregiver_alert_toggle(self, caregiver_id: int, enabled: bool) -> Caregiver | None:
        with self.connect() as conn:
            conn.execute(
                "UPDATE caregivers SET accepts_lost_pet_alerts = ? WHERE id = ?",
                (int(enabled), caregiver_id),
            )
        return self.get_caregiver(caregiver_id)

    def get_caregiver(self, caregiver_id: int) -> Caregiver | None:
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM caregivers WHERE id = ?", (caregiver_id,)).fetchone()
            return self._row_to_caregiver(row) if row else None

    def list_caregivers(self, limit: int = 50) -> list[Caregiver]:
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM caregivers ORDER BY id DESC LIMIT ?", (limit,)).fetchall()
            return [self._row_to_caregiver(row) for row in rows]

    def count_nearby_users(self, location: Location, radius_meters: int) -> int:
        # Simulación determinista: suficiente para demo local y pruebas unitarias.
        base = int(abs(location.latitude * 1000) + abs(location.longitude * 1000))
        return max(1, min(250, (base % 40) + radius_meters // 100))

    def search_adoptions(self, species: str | None, breed: str | None) -> list[SearchResult]:
        query = "SELECT * FROM adoption_catalog WHERE 1 = 1"
        params: list[str] = []
        if species:
            query += " AND species = ?"
            params.append(species.lower())
        if breed:
            query += " AND breed = ?"
            params.append(breed.lower())
        with self.connect() as conn:
            rows = conn.execute(query, params).fetchall()
        return [
            SearchResult(
                source="ONG/Protectora",
                title=row["title"],
                description=row["description"],
                score=0.91,
                certified=True,
                location=row["location"],
            )
            for row in rows
        ]

    def search_certified_breeders(self, species: str | None, breed: str | None) -> list[SearchResult]:
        query = "SELECT * FROM breeder_catalog WHERE certified = 1"
        params: list[str] = []
        if species:
            query += " AND species = ?"
            params.append(species.lower())
        if breed:
            query += " AND breed = ?"
            params.append(breed.lower())
        with self.connect() as conn:
            rows = conn.execute(query, params).fetchall()
        return [
            SearchResult(
                source="Criadero certificado",
                title=row["title"],
                description=row["description"],
                score=0.88,
                certified=bool(row["certified"]),
                location=row["location"],
            )
            for row in rows
        ]

    def search_active_lost_alerts(self, species: str | None, breed: str | None) -> list[SearchResult]:
        query = "SELECT * FROM reports WHERE report_type = ? AND status = ?"
        params: list[str] = [ReportType.LOST_PET.value, ReportStatus.ACTIVE.value]
        if species:
            query += " AND species = ?"
            params.append(species.lower())
        if breed:
            query += " AND breed = ?"
            params.append(breed.lower())
        with self.connect() as conn:
            rows = conn.execute(query, params).fetchall()
        return [
            SearchResult(
                source="Alerta activa",
                title=f"Mascota perdida: {row['pet_name']}",
                description=row["description"],
                score=0.94,
                certified=True,
                location=f"{row['latitude']:.5f}, {row['longitude']:.5f}",
            )
            for row in rows
        ]

    def _row_to_report(self, row: sqlite3.Row) -> Report:
        owner = None
        if row["owner_name"]:
            owner = Owner(row["owner_name"], row["owner_email"] or "", row["owner_phone"] or "")
        return Report(
            id=int(row["id"]),
            report_type=ReportType(row["report_type"]),
            pet=Pet(row["pet_name"], row["species"], row["breed"], row["description"], row["photo_path"]),
            description=row["description"],
            location=Location(float(row["latitude"]), float(row["longitude"])),
            owner=owner,
            reporter_alias=row["reporter_alias"],
            status=ReportStatus(row["status"]),
            radius_meters=int(row["radius_meters"]),
        )

    def _row_to_caregiver(self, row: sqlite3.Row) -> Caregiver:
        return Caregiver(
            id=int(row["id"]),
            full_name=row["full_name"],
            dni=row["dni"],
            role=CaregiverRole(row["role"]),
            species_accepted=row["species_accepted"],
            size_accepted=row["size_accepted"],
            medication=bool(row["medication"]),
            accepts_lost_pet_alerts=bool(row["accepts_lost_pet_alerts"]),
            status=CaregiverStatus(row["status"]),
            average_rating=float(row["average_rating"]),
        )
