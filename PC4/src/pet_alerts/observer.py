from __future__ import annotations

from abc import ABC, abstractmethod
from collections import defaultdict
from typing import Any, DefaultDict


class Observer(ABC):
    @abstractmethod
    def update(self, event_name: str, payload: dict[str, Any]) -> None:
        raise NotImplementedError


class EventBus:
    """Observer/Event Bus para notificaciones reactivas."""

    def __init__(self) -> None:
        self._subscribers: DefaultDict[str, list[Observer]] = defaultdict(list)

    def subscribe(self, event_name: str, observer: Observer) -> None:
        self._subscribers[event_name].append(observer)

    def publish(self, event_name: str, payload: dict[str, Any]) -> None:
        for observer in self._subscribers[event_name]:
            observer.update(event_name, payload)


class HistoryObserver(Observer):
    def __init__(self) -> None:
        self.events: list[tuple[str, dict[str, Any]]] = []

    def update(self, event_name: str, payload: dict[str, Any]) -> None:
        self.events.append((event_name, payload))


class ConsoleNotificationObserver(Observer):
    def __init__(self) -> None:
        self.messages: list[str] = []

    def update(self, event_name: str, payload: dict[str, Any]) -> None:
        self.messages.append(f"{event_name}: {payload.get('summary', '')}")
