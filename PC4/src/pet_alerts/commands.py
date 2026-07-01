from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class Command(ABC):
    """Command para encapsular casos de uso y facilitar auditoría/reintentos."""

    @abstractmethod
    def execute(self) -> Any:
        raise NotImplementedError


class RegisterLostPetCommand(Command):
    def __init__(self, facade, data: dict[str, Any], photo_file=None, filename: str | None = None):
        self.facade = facade
        self.data = data
        self.photo_file = photo_file
        self.filename = filename

    def execute(self) -> Any:
        return self.facade.register_lost_pet(self.data, self.photo_file, self.filename)


class RegisterSightingCommand(Command):
    def __init__(self, facade, data: dict[str, Any], photo_file=None, filename: str | None = None):
        self.facade = facade
        self.data = data
        self.photo_file = photo_file
        self.filename = filename

    def execute(self) -> Any:
        return self.facade.register_sighting(self.data, self.photo_file, self.filename)


class SearchByImageCommand(Command):
    def __init__(self, facade, intent: str, metadata_json: str | None):
        self.facade = facade
        self.intent = intent
        self.metadata_json = metadata_json

    def execute(self) -> Any:
        return self.facade.search(self.intent, self.metadata_json)


class RegisterCaregiverCommand(Command):
    def __init__(self, facade, data: dict[str, Any]):
        self.facade = facade
        self.data = data

    def execute(self) -> Any:
        return self.facade.register_caregiver(self.data)


class ToggleCaregiverAlertsCommand(Command):
    def __init__(self, facade, caregiver_id: int, enabled: bool):
        self.facade = facade
        self.caregiver_id = caregiver_id
        self.enabled = enabled

    def execute(self) -> Any:
        return self.facade.toggle_alerts(self.caregiver_id, self.enabled)
