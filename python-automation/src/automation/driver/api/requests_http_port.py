from typing import Any

import requests

from automation.driver.ports.http_port import HttpPort


class RequestsHttpPort(HttpPort):

    def __init__(self, base_url: str) -> None:
        self._base_url = base_url.rstrip("/")
        self._session = requests.Session()

    def get_as(self, path: str) -> dict[str, Any]:
        response = self._session.get(self._url(path), timeout=10)
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, dict):
            raise TypeError(f"Expected JSON object from {path}, got {type(body).__name__}")
        return body

    def get_list_as(self, path: str) -> list[dict[str, Any]]:
        response = self._session.get(self._url(path), timeout=10)
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, list):
            raise TypeError(f"Expected JSON array from {path}, got {type(body).__name__}")
        return body

    def get_list_with_query_as(
        self, path: str, param_name: str, param_value: str
    ) -> list[dict[str, Any]]:
        response = self._session.get(
            self._url(path), params={param_name: param_value}, timeout=10
        )
        response.raise_for_status()
        body = response.json()
        if not isinstance(body, list):
            raise TypeError(f"Expected JSON array from {path}, got {type(body).__name__}")
        return body

    def close(self) -> None:
        self._session.close()

    def _url(self, path: str) -> str:
        if path.startswith("http"):
            return path
        return f"{self._base_url}{path if path.startswith('/') else '/' + path}"
