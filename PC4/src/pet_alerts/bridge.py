from __future__ import annotations

from abc import ABC, abstractmethod

from .models import Alert


class NotificationChannel(ABC):
    """Bridge: separa la alerta del canal por donde se distribuye."""

    name: str

    @abstractmethod
    def send(self, alert: Alert) -> str:
        raise NotImplementedError


class PushNotificationChannel(NotificationChannel):
    name = "push"

    def send(self, alert: Alert) -> str:
        return f"Push enviada a {alert.recipients_count} usuarios."


class EmailNotificationChannel(NotificationChannel):
    name = "email"

    def send(self, alert: Alert) -> str:
        return f"Email enviado a {alert.recipients_count} usuarios registrados."


class SmsNotificationChannel(NotificationChannel):
    name = "sms"

    def send(self, alert: Alert) -> str:
        return f"SMS enviado a {alert.recipients_count} usuarios críticos."


class AlertNotifier:
    def __init__(self, channels: list[NotificationChannel]):
        self.channels = channels

    def notify(self, alert: Alert) -> list[str]:
        return [channel.send(alert) for channel in self.channels]
