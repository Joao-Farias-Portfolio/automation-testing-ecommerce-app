from typing import Any

import httpx

from automation.driver.ports.http_port import HttpPort


class HttpxHttpPort(HttpPort):

    def __init__(self, base_url: str) -> None:
        self._client = httpx.Client(base_url=base_url, timeout=10.0)

    def get_as(self, path: str) -> dict[str, Any]:
        response = self._client.get(path)
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, dict):
            raise TypeError(f"Expected JSON object from {path}, got {type(body).__name__}")
        return body

    def get_list_as(self, path: str) -> list[dict[str, Any]]:
        response = self._client.get(path)
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, list):
            raise TypeError(f"Expected JSON array from {path}, got {type(body).__name__}")
        return body

    def get_list_with_query_as(
        self, path: str, param_name: str, param_value: str
    ) -> list[dict[str, Any]]:
        response = self._client.get(path, params={param_name: param_value})
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, list):
            raise TypeError(f"Expected JSON array from {path}, got {type(body).__name__}")
        return body

    def close(self) -> None:
        self._client.close()
