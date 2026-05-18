from typing import Optional

from automation.driver.ports.http_port import HttpPort

_active: Optional[HttpPort] = None


def set_http(http: HttpPort) -> None:
    global _active
    _active = http


def get_http() -> HttpPort:
    if _active is None:
        raise RuntimeError(
            "No HTTP client is active. The behave environment.py must call set_http() "
            "before scenarios run."
        )
    return _active


def clear_http() -> None:
    global _active
    if _active is not None:
        try:
            _active.close()
        except Exception:
            pass
    _active = None
