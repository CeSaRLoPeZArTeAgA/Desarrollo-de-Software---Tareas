from __future__ import annotations

from .models import SearchResult


class SearchResultDecorator:
    """Decorator base para filtros agregables sobre resultados de búsqueda."""

    def __init__(self, results: list[SearchResult]):
        self._results = results

    def apply(self) -> list[SearchResult]:
        return self._results


class CertifiedOnlyDecorator(SearchResultDecorator):
    def apply(self) -> list[SearchResult]:
        return [result for result in self._results if result.certified]


class SourceOnlyDecorator(SearchResultDecorator):
    def __init__(self, results: list[SearchResult], source: str):
        super().__init__(results)
        self.source = source

    def apply(self) -> list[SearchResult]:
        return [result for result in self._results if result.source == self.source]
