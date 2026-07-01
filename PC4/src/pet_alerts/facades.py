from __future__ import annotations

import time
from typing import BinaryIO

from .adapters import IdentityProviderAdapter, ImageStorageAdapter, JsonMetadataAdapter
from .bridge import AlertNotifier, EmailNotificationChannel, PushNotificationChannel
from .builders import AlertBuilder
from .chain import (
    CoordinatesValidator,
    DniValidator,
    ImageExtensionValidator,
    RadiusValidator,
    RequiredFieldsValidator,
)
from .config import SettingsSingleton
from .decorators import CertifiedOnlyDecorator, SourceOnlyDecorator
from .factories import CaregiverFactory, LostPetReportFactory, SearchFamilyFactory, SightingReportFactory
from .models import Caregiver, Report, SearchIntent, SearchResult
from .observer import EventBus
from .proxy import OwnerPrivacyProxy, OwnerPublicView
from .repository import SQLiteRepository
from .states import PendingCaregiverState, VerifiedCaregiverState


class AlertMascotaFacade:
    """Facade del módulo de reportes y alertas."""

    def __init__(self, repository: SQLiteRepository, event_bus: EventBus):
        self.repository = repository
        self.event_bus = event_bus
        settings = SettingsSingleton.get()
        self.storage = ImageStorageAdapter(settings.upload_dir)
        self.notifier = AlertNotifier([PushNotificationChannel(), EmailNotificationChannel()])

    def register_lost_pet(self, data: dict, photo_file: BinaryIO | None = None, filename: str | None = None) -> dict:
        RequiredFieldsValidator(
            ["pet_name", "species", "breed", "description", "latitude", "longitude", "owner_name", "owner_email", "owner_phone"],
            CoordinatesValidator(RadiusValidator(ImageExtensionValidator())),
        ).validate({**data, "filename": filename})

        photo_path = self.storage.save(photo_file, filename)
        report_data = dict(data)
        report_data["radius_meters"] = int(
            report_data.get("radius_meters", SettingsSingleton.get().default_alert_radius_meters)
        )
        report = LostPetReportFactory().create(
            **report_data,
            photo_path=photo_path,
        )
        report = self.repository.save_report(report)
        alert_summary = self._dispatch_alert(report)
        owner_view = OwnerPrivacyProxy(report.owner).public_view() if report.owner else None
        self.event_bus.publish(
            "lost_pet_registered",
            {"report_id": report.id, "summary": f"Mascota perdida registrada: {report.pet.name}"},
        )
        return {
            "report": report,
            "alert": alert_summary,
            "owner_public_view": owner_view,
        }

    def register_sighting(self, data: dict, photo_file: BinaryIO | None = None, filename: str | None = None) -> Report:
        RequiredFieldsValidator(
            ["species", "description", "latitude", "longitude"],
            CoordinatesValidator(ImageExtensionValidator()),
        ).validate({**data, "filename": filename})
        photo_path = self.storage.save(photo_file, filename)
        report = SightingReportFactory().create(**data, photo_path=photo_path)
        report = self.repository.save_report(report)
        self.event_bus.publish(
            "sighting_registered",
            {"report_id": report.id, "summary": f"Avistamiento registrado: {report.pet.species}"},
        )
        return report

    def _dispatch_alert(self, report: Report) -> dict:
        start = time.perf_counter()
        recipients = self.repository.count_nearby_users(report.location, report.radius_meters)
        latency = time.perf_counter() - start
        alert = (
            AlertBuilder()
            .with_report(report)
            .with_owner_anonymization(True)
            .with_recipients_count(recipients)
            .with_channel("push")
            .with_channel("email")
            .with_latency(latency)
            .build()
        )
        messages = self.notifier.notify(alert)
        self.repository.save_alert_event(report.id or 0, recipients, alert.channels, latency)
        return {
            "recipients_count": recipients,
            "channels": alert.channels,
            "latency_seconds": latency,
            "latency_ok": latency < SettingsSingleton.get().alert_latency_limit_seconds,
            "messages": messages,
        }

    def recent_reports(self) -> list[Report]:
        return self.repository.list_recent_reports()


class BusquedaImagenFacade:
    """Facade del buscador multipropósito por imagen."""

    def __init__(self, repository: SQLiteRepository):
        self.repository = repository

    def search(self, intent: str, metadata_json: str | None) -> list[SearchResult]:
        parsed_intent = SearchIntent(intent)
        metadata = JsonMetadataAdapter.parse(metadata_json)
        strategy = SearchFamilyFactory.create_strategy(parsed_intent)
        results = strategy.search(self.repository, metadata)

        if parsed_intent == SearchIntent.ADOPTION:
            results = SourceOnlyDecorator(results, "ONG/Protectora").apply()
        elif parsed_intent == SearchIntent.SALE:
            results = CertifiedOnlyDecorator(results).apply()
            results = SourceOnlyDecorator(results, "Criadero certificado").apply()
        elif parsed_intent == SearchIntent.VERIFY_LOSS:
            results = SourceOnlyDecorator(results, "Alerta activa").apply()
        return results


class CuidadorFacade:
    """Facade del módulo de red de cuidadores."""

    def __init__(self, repository: SQLiteRepository):
        self.repository = repository
        self.identity_provider = IdentityProviderAdapter()

    def register_caregiver(self, data: dict) -> Caregiver:
        RequiredFieldsValidator(
            ["full_name", "dni", "role", "species_accepted", "size_accepted"],
            DniValidator(),
        ).validate(data)
        caregiver = CaregiverFactory.create(**data)
        if self.identity_provider.validate_dni(caregiver.dni):
            VerifiedCaregiverState().apply(caregiver)
        else:
            PendingCaregiverState().apply(caregiver)
        return self.repository.save_caregiver(caregiver)

    def toggle_alerts(self, caregiver_id: int, enabled: bool) -> Caregiver | None:
        return self.repository.update_caregiver_alert_toggle(caregiver_id, enabled)

    def list_caregivers(self) -> list[Caregiver]:
        return self.repository.list_caregivers()
