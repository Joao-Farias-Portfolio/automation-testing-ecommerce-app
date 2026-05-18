from abc import ABC, abstractmethod

from automation.dsl.domain import (
    DeliveryState,
    ProductDetail,
    ProductListing,
    SearchResults,
)


class CatalogueProtocol(ABC):

    @abstractmethod
    def browse_catalogue(self) -> None: ...

    @abstractmethod
    def view_first_product(self) -> None: ...

    @abstractmethod
    def search_for(self, term: str) -> None: ...

    @abstractmethod
    def return_to_product_listing(self) -> None: ...

    @abstractmethod
    def choose_alternative_delivery_option(self) -> None: ...

    @abstractmethod
    def get_product_listing(self) -> ProductListing: ...

    @abstractmethod
    def get_product_detail(self) -> ProductDetail: ...

    @abstractmethod
    def get_delivery_state(self) -> DeliveryState: ...

    @abstractmethod
    def get_search_results(self) -> SearchResults: ...

    @abstractmethod
    def current_url(self) -> str: ...
