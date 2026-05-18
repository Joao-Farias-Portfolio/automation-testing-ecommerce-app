import os
from enum import Enum
from typing import Optional


class Channel(Enum):
    WEB = "Web"
    API = "API"


_cached: Optional[Channel] = None


def current_channel() -> Channel:
    global _cached
    if _cached is None:
        _cached = _resolve()
    return _cached


def reset_cached_channel() -> None:
    global _cached
    _cached = None


def _resolve() -> Channel:
    raw = os.environ.get("CHANNEL", "").strip()
    if not raw:
        raise RuntimeError(
            "Environment variable CHANNEL is required. Valid values: Web, API"
        )
    try:
        return Channel[raw.upper()]
    except KeyError as e:
        raise RuntimeError(
            f"Unknown channel '{raw}'. Valid values: Web, API"
        ) from e
