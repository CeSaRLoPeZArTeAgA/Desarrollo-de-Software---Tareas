from __future__ import annotations

from .models import Alert, Report


class AlertBuilder:
    """Builder para construir alertas complejas sin constructores extensos."""

    def __init__(self) -> None:
        self._report: Report | None = None
        self._anonymized_owner = True
        self._recipients_count = 0
        self._channels: list[str] = []
        self._latency_seconds = 0.0

    def with_report(self, report: Report) -> "AlertBuilder":
        self._report = report
        return self

    def with_owner_anonymization(self, enabled: bool) -> "AlertBuilder":
        self._anonymized_owner = enabled
        return self

    def with_recipients_count(self, count: int) -> "AlertBuilder":
        self._recipients_count = count
        return self

    def with_channel(self, channel: str) -> "AlertBuilder":
        self._channels.append(channel)
        return self

    def with_latency(self, latency_seconds: float) -> "AlertBuilder":
        self._latency_seconds = latency_seconds
        return self

    def build(self) -> Alert:
        if self._report is None:
            raise ValueError("La alerta requiere un reporte asociado.")
        if not self._channels:
            raise ValueError("La alerta requiere al menos un canal de notificación.")
        return Alert(
            report=self._report,
            anonymized_owner=self._anonymized_owner,
            recipients_count=self._recipients_count,
            channels=self._channels,
            latency_seconds=self._latency_seconds,
        )
