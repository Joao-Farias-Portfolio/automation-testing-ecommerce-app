from automation.dsl.protocols import driver_registry
from automation.dsl.protocols.cart_protocol import CartProtocol
from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol
from automation.dsl.protocols.channel import current_channel
from automation.dsl.protocols.saved_protocol import SavedProtocol


def create_catalogue() -> CatalogueProtocol:
    return driver_registry.create(current_channel())


def create_cart() -> CartProtocol:
    driver = driver_registry.create(current_channel())
    if not isinstance(driver, CartProtocol):
        raise RuntimeError(
            f"Channel '{current_channel().value}' does not support cart operations"
        )
    return driver


def create_saved() -> SavedProtocol:
    driver = driver_registry.create(current_channel())
    if not isinstance(driver, SavedProtocol):
        raise RuntimeError(
            f"Channel '{current_channel().value}' does not support saved/wishlist operations"
        )
    return driver
