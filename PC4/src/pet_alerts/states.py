from __future__ import annotations

from abc import ABC, abstractmethod

from .models import Caregiver, CaregiverStatus, Report, ReportStatus


class ReportState(ABC):
    @abstractmethod
    def apply(self, report: Report) -> None:
        raise NotImplementedError


class ActiveReportState(ReportState):
    def apply(self, report: Report) -> None:
        report.status = ReportStatus.ACTIVE


class PossibleMatchState(ReportState):
    def apply(self, report: Report) -> None:
        report.status = ReportStatus.POSSIBLE_MATCH


class ResolvedReportState(ReportState):
    def apply(self, report: Report) -> None:
        report.status = ReportStatus.RESOLVED


class CaregiverState(ABC):
    @abstractmethod
    def apply(self, caregiver: Caregiver) -> None:
        raise NotImplementedError


class VerifiedCaregiverState(CaregiverState):
    def apply(self, caregiver: Caregiver) -> None:
        caregiver.status = CaregiverStatus.VERIFIED


class PendingCaregiverState(CaregiverState):
    def apply(self, caregiver: Caregiver) -> None:
        caregiver.status = CaregiverStatus.PENDING_VALIDATION


class RejectedCaregiverState(CaregiverState):
    def apply(self, caregiver: Caregiver) -> None:
        caregiver.status = CaregiverStatus.REJECTED
