from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class ValidationError(ValueError):
    pass


class Validator(ABC):
    """Chain of Responsibility para validaciones de entrada."""

    def __init__(self, next_validator: "Validator | None" = None):
        self._next = next_validator

    def validate(self, data: dict[str, Any]) -> None:
        self._validate_current(data)
        if self._next:
            self._next.validate(data)

    @abstractmethod
    def _validate_current(self, data: dict[str, Any]) -> None:
        raise NotImplementedError


class RequiredFieldsValidator(Validator):
    def __init__(self, fields: list[str], next_validator: Validator | None = None):
        super().__init__(next_validator)
        self.fields = fields

    def _validate_current(self, data: dict[str, Any]) -> None:
        missing = [field for field in self.fields if data.get(field) in (None, "")]
        if missing:
            raise ValidationError(f"Faltan campos obligatorios: {', '.join(missing)}")


class CoordinatesValidator(Validator):
    def _validate_current(self, data: dict[str, Any]) -> None:
        try:
            latitude = float(data["latitude"])
            longitude = float(data["longitude"])
        except (KeyError, TypeError, ValueError) as exc:
            raise ValidationError("Las coordenadas deben ser numéricas.") from exc
        if not -90 <= latitude <= 90:
            raise ValidationError("La latitud debe estar entre -90 y 90.")
        if not -180 <= longitude <= 180:
            raise ValidationError("La longitud debe estar entre -180 y 180.")


class RadiusValidator(Validator):
    def _validate_current(self, data: dict[str, Any]) -> None:
        radius = int(data.get("radius_meters", 1000))
        if radius < 100 or radius > 10000:
            raise ValidationError("El radio debe estar entre 100 y 10000 metros.")


class DniValidator(Validator):
    def _validate_current(self, data: dict[str, Any]) -> None:
        dni = str(data.get("dni", ""))
        if not dni.isdigit() or len(dni) != 8:
            raise ValidationError("El DNI debe tener exactamente 8 dígitos.")


class ImageExtensionValidator(Validator):
    def _validate_current(self, data: dict[str, Any]) -> None:
        filename = data.get("filename")
        if filename:
            lower = str(filename).lower()
            if not (lower.endswith(".jpg") or lower.endswith(".jpeg") or lower.endswith(".png")):
                raise ValidationError("La imagen debe tener formato JPEG o PNG.")
