from __future__ import annotations

import json
import shutil
import uuid
from pathlib import Path
from typing import BinaryIO

from .models import Location, SearchMetadata


class ImageStorageAdapter:
    """Adapter para desacoplar la API de FastAPI del almacenamiento real de archivos."""

    allowed_extensions = {".jpg", ".jpeg", ".png"}

    def __init__(self, upload_dir: Path):
        self.upload_dir = upload_dir
        self.upload_dir.mkdir(parents=True, exist_ok=True)

    def save(self, file_obj: BinaryIO | None, original_filename: str | None) -> str | None:
        if file_obj is None or not original_filename:
            return None
        suffix = Path(original_filename).suffix.lower()
        if suffix not in self.allowed_extensions:
            raise ValueError("Solo se permiten imágenes JPEG o PNG.")
        safe_name = f"{uuid.uuid4().hex}{suffix}"
        target = self.upload_dir / safe_name
        with target.open("wb") as output:
            shutil.copyfileobj(file_obj, output)
        return f"/uploads/{safe_name}"


class GPSMapAdapter:
    """Adapter común para coordenadas recibidas desde GPS móvil o mapa web."""

    @staticmethod
    def from_lat_lng(latitude: float, longitude: float) -> Location:
        if not -90 <= latitude <= 90:
            raise ValueError("La latitud debe estar entre -90 y 90.")
        if not -180 <= longitude <= 180:
            raise ValueError("La longitud debe estar entre -180 y 180.")
        return Location(latitude=latitude, longitude=longitude)


class JsonMetadataAdapter:
    """Adapter RNF 2.1: convierte JSON estándar en metadatos internos."""

    @staticmethod
    def parse(metadata_json: str | None) -> SearchMetadata:
        if not metadata_json:
            return SearchMetadata()
        try:
            raw = json.loads(metadata_json)
        except json.JSONDecodeError as exc:
            raise ValueError("El campo metadata_json debe ser JSON válido.") from exc

        location = None
        if "latitude" in raw and "longitude" in raw:
            location = GPSMapAdapter.from_lat_lng(float(raw["latitude"]), float(raw["longitude"]))
        return SearchMetadata(
            species=str(raw.get("species", "")).lower() or None,
            breed=str(raw.get("breed", "")).lower() or None,
            location=location,
            raw=raw,
        )


class IdentityProviderAdapter:
    """Adapter de validación documental.

    En producción se conectaría con un proveedor externo. Para la PC, se usa una
    validación determinista: DNI de 8 dígitos y último dígito par => verificado.
    """

    @staticmethod
    def validate_dni(dni: str) -> bool:
        return dni.isdigit() and len(dni) == 8 and int(dni[-1]) % 2 == 0
