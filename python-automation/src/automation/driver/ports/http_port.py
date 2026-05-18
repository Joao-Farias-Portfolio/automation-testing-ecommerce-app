from abc import ABC, abstractmethod
from typing import Any


class HttpPort(ABC):

    @abstractmethod
    def get_as(self, path: str) -> dict[str, Any]: ...

    @abstractmethod
    def get_list_as(self, path: str) -> list[dict[str, Any]]: ...

    @abstractmethod
    def get_list_with_query_as(
        self, path: str, param_name: str, param_value: str
    ) -> list[dict[str, Any]]: ...

    @abstractmethod
    def close(self) -> None: ...
