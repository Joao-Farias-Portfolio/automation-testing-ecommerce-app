from typing import Optional

from automation.dsl.protocols import driver_registry
from automation.dsl.protocols.cart_protocol import CartProtocol
from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol
from automation.dsl.protocols.channel import current_channel
from automation.dsl.protocols.saved_protocol import SavedProtocol

_active: Optional[CatalogueProtocol] = None


def create_catalogue() -> CatalogueProtocol:
    return _get_or_build()


def create_cart() -> CartProtocol:
    driver = _get_or_build()
    if not isinstance(driver, CartProtocol):
        raise RuntimeError(
            f"Channel '{current_channel().value}' does not support cart operations"
        )
    return driver


def create_saved() -> SavedProtocol:
    driver = _get_or_build()
    if not isinstance(driver, SavedProtocol):
        raise RuntimeError(
            f"Channel '{current_channel().value}' does not support saved/wishlist operations"
        )
    return driver


def reset_active_driver() -> None:
    """Clear the cached driver. Call this between scenarios so the next one
    gets a fresh instance (mirrors java-automation's per-step-class field init)."""
    global _active
    _active = None


def _get_or_build() -> CatalogueProtocol:
    global _active
    if _active is None:
        _active = driver_registry.create(current_channel())
    return _active
