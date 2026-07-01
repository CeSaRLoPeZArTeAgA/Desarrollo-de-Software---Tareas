from __future__ import annotations

from abc import ABC, abstractmethod

from .models import SearchMetadata, SearchResult


class SearchStrategy(ABC):
    """Strategy para cambiar el algoritmo de búsqueda según la intención."""

    @abstractmethod
    def search(self, repository, metadata: SearchMetadata) -> list[SearchResult]:
        raise NotImplementedError


class AdoptionSearchStrategy(SearchStrategy):
    def search(self, repository, metadata: SearchMetadata) -> list[SearchResult]:
        return repository.search_adoptions(metadata.species, metadata.breed)


class SaleSearchStrategy(SearchStrategy):
    def search(self, repository, metadata: SearchMetadata) -> list[SearchResult]:
        return repository.search_certified_breeders(metadata.species, metadata.breed)


class VerifyLossSearchStrategy(SearchStrategy):
    def search(self, repository, metadata: SearchMetadata) -> list[SearchResult]:
        return repository.search_active_lost_alerts(metadata.species, metadata.breed)
