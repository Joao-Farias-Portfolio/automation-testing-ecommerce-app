from abc import abstractmethod

from automation.dsl.domain import SavedState
from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol


class SavedProtocol(CatalogueProtocol):

    @abstractmethod
    def view_saved_items(self) -> None: ...

    @abstractmethod
    def ensure_first_product_is_saved(self) -> None: ...

    @abstractmethod
    def toggle_save_state_of_first_product(self) -> None: ...

    @abstractmethod
    def view_wishlist(self) -> None: ...

    @abstractmethod
    def get_saved_state(self) -> SavedState: ...
