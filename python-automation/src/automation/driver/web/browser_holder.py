from typing import Optional

from automation.driver.ports.browser_port import BrowserPort

_active: Optional[BrowserPort] = None


def set_browser(browser: BrowserPort) -> None:
    global _active
    _active = browser


def get_browser() -> BrowserPort:
    if _active is None:
        raise RuntimeError(
            "No browser is active. The behave environment.py must call set_browser() "
            "before scenarios run."
        )
    return _active


def clear_browser() -> None:
    global _active
    if _active is not None:
        try:
            _active.close()
        except Exception:
            pass
    _active = None
