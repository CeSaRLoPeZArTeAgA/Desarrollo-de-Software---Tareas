from __future__ import annotations

from dataclasses import dataclass

from .models import Owner


@dataclass(slots=True)
class OwnerPublicView:
    display_name: str
    contact_channel: str


class OwnerPrivacyProxy:
    """Proxy para cumplir RNF 1.2: anonimiza datos personales del dueño."""

    def __init__(self, owner: Owner):
        self._owner = owner

    def public_view(self) -> OwnerPublicView:
        return OwnerPublicView(
            display_name="Dueño protegido por la plataforma",
            contact_channel="Canal interno seguro: responder desde la aplicación",
        )

    def internal_owner(self) -> Owner:
        return self._owner
