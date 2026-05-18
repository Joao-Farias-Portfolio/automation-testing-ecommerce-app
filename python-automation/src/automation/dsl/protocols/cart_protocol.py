from abc import abstractmethod

from automation.dsl.domain import CartState
from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol


class CartProtocol(CatalogueProtocol):

    @abstractmethod
    def view_cart(self) -> None: ...

    @abstractmethod
    def add_product_to_cart(self) -> None: ...

    @abstractmethod
    def remove_first_item_from_cart(self) -> None: ...

    @abstractmethod
    def change_quantity_to(self, quantity: int) -> None: ...

    @abstractmethod
    def get_cart_state(self) -> CartState: ...
