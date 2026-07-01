from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from enum import Enum
from typing import Any


class ReportType(str, Enum):
    LOST_PET = "lost_pet"
    SIGHTING = "sighting"


class ReportStatus(str, Enum):
    ACTIVE = "active"
    VERIFYING = "verifying"
    POSSIBLE_MATCH = "possible_match"
    RESOLVED = "resolved"
    EXPIRED = "expired"


class SearchIntent(str, Enum):
    ADOPTION = "adoption"
    SALE = "sale"
    VERIFY_LOSS = "verify_loss"


class CaregiverRole(str, Enum):
    SOLIDARY = "solidary"
    PROFESSIONAL = "professional"
    SPECIALIZED = "specialized"


class CaregiverStatus(str, Enum):
    PENDING_VALIDATION = "pending_validation"
    VERIFIED = "verified"
    REJECTED = "rejected"
    SUSPENDED = "suspended"


@dataclass(slots=True)
class Location:
    latitude: float
    longitude: float


@dataclass(slots=True)
class Pet:
    name: str
    species: str
    breed: str
    description: str
    photo_path: str | None = None


@dataclass(slots=True)
class Owner:
    full_name: str
    email: str
    phone: str


@dataclass(slots=True)
class Report:
    report_type: ReportType
    pet: Pet
    location: Location
    description: str
    owner: Owner | None = None
    reporter_alias: str | None = None
    id: int | None = None
    status: ReportStatus = ReportStatus.ACTIVE
    radius_meters: int = 1000
    created_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))


@dataclass(slots=True)
class Alert:
    report: Report
    anonymized_owner: bool
    recipients_count: int
    channels: list[str]
    latency_seconds: float = 0.0


@dataclass(slots=True)
class SearchMetadata:
    species: str | None = None
    breed: str | None = None
    location: Location | None = None
    raw: dict[str, Any] = field(default_factory=dict)


@dataclass(slots=True)
class SearchResult:
    source: str
    title: str
    description: str
    score: float
    certified: bool = True
    location: str | None = None


@dataclass(slots=True)
class Caregiver:
    full_name: str
    dni: str
    role: CaregiverRole
    species_accepted: str
    size_accepted: str
    medication: bool
    accepts_lost_pet_alerts: bool = True
    status: CaregiverStatus = CaregiverStatus.PENDING_VALIDATION
    average_rating: float = 0.0
    id: int | None = None
