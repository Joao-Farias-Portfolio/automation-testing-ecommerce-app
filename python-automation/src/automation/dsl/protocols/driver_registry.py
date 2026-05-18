import importlib
from collections.abc import Callable

from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol
from automation.dsl.protocols.channel import Channel

_registry: dict[Channel, Callable[[], CatalogueProtocol]] = {}


def register(channel: Channel, factory: Callable[[], CatalogueProtocol]) -> None:
    _registry[channel] = factory


def create(channel: Channel) -> CatalogueProtocol:
    factory = _registry.get(channel)
    if factory is None:
        _load_by_convention(channel)
        factory = _registry.get(channel)
    if factory is None:
        raise RuntimeError(
            f"No driver registered for channel '{channel.value}'. "
            f"Registered: {[c.value for c in _registry.keys()]}"
        )
    return factory()


def _load_by_convention(channel: Channel) -> None:
    module_name = f"automation.driver.{channel.name.lower()}.my_ecommerce_driver"
    try:
        importlib.import_module(module_name)
    except ImportError:
        pass
