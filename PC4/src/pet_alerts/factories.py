from __future__ import annotations

from abc import ABC, abstractmethod

from .models import (
    Caregiver,
    CaregiverRole,
    Location,
    Owner,
    Pet,
    Report,
    ReportType,
    SearchIntent,
)
from .strategies import AdoptionSearchStrategy, SaleSearchStrategy, SearchStrategy, VerifyLossSearchStrategy


def _to_bool(value) -> bool:
    if isinstance(value, bool):
        return value
    if value is None:
        return False
    if isinstance(value, (int, float)):
        return bool(value)
    return str(value).strip().lower() in {"1", "true", "yes", "on", "si", "sí"}


class ReportFactory(ABC):
    """Factory Method para crear reportes de dominio."""

    @abstractmethod
    def create(self, **kwargs) -> Report:
        raise NotImplementedError


class LostPetReportFactory(ReportFactory):
    def create(self, **kwargs) -> Report:
        pet = Pet(
            name=kwargs["pet_name"],
            species=kwargs["species"],
            breed=kwargs["breed"],
            description=kwargs["description"],
            photo_path=kwargs.get("photo_path"),
        )
        owner = Owner(
            full_name=kwargs["owner_name"],
            email=kwargs["owner_email"],
            phone=kwargs["owner_phone"],
        )
        return Report(
            report_type=ReportType.LOST_PET,
            pet=pet,
            location=Location(float(kwargs["latitude"]), float(kwargs["longitude"])),
            description=kwargs["description"],
            owner=owner,
            radius_meters=int(kwargs["radius_meters"]),
        )


class SightingReportFactory(ReportFactory):
    def create(self, **kwargs) -> Report:
        pet = Pet(
            name=kwargs.get("pet_name", "Mascota avistada"),
            species=kwargs["species"],
            breed=kwargs.get("breed", "desconocida"),
            description=kwargs["description"],
            photo_path=kwargs.get("photo_path"),
        )
        return Report(
            report_type=ReportType.SIGHTING,
            pet=pet,
            location=Location(float(kwargs["latitude"]), float(kwargs["longitude"])),
            description=kwargs["description"],
            reporter_alias=kwargs.get("reporter_alias", "Ciudadano anónimo"),
        )


class CaregiverFactory:
    """Factory Method para crear perfiles de cuidadores según rol."""

    @staticmethod
    def create(**kwargs) -> Caregiver:
        return Caregiver(
            full_name=kwargs["full_name"],
            dni=kwargs["dni"],
            role=CaregiverRole(kwargs["role"]),
            species_accepted=kwargs["species_accepted"],
            size_accepted=kwargs["size_accepted"],
            medication=_to_bool(kwargs.get("medication", False)),
            accepts_lost_pet_alerts=_to_bool(kwargs.get("accepts_lost_pet_alerts", True)),
        )


class SearchFamilyFactory:
    """Abstract Factory simplificada: crea la familia de estrategia/filtros por intención."""

    @staticmethod
    def create_strategy(intent: SearchIntent) -> SearchStrategy:
        if intent == SearchIntent.ADOPTION:
            return AdoptionSearchStrategy()
        if intent == SearchIntent.SALE:
            return SaleSearchStrategy()
        if intent == SearchIntent.VERIFY_LOSS:
            return VerifyLossSearchStrategy()
        raise ValueError(f"Intención de búsqueda no soportada: {intent}")
