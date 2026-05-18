from abc import ABC, abstractmethod
from collections.abc import Callable
from typing import Any


class BrowserPort(ABC):

    @abstractmethod
    def navigate_to(self, url: str) -> None: ...

    @abstractmethod
    def navigate_back(self) -> None: ...

    @abstractmethod
    def current_url(self) -> str: ...

    @abstractmethod
    def is_visible(self, css: str) -> bool: ...

    @abstractmethod
    def is_present(self, css: str) -> bool: ...

    @abstractmethod
    def is_enabled(self, css: str) -> bool: ...

    @abstractmethod
    def is_selected(self, css: str) -> bool: ...

    @abstractmethod
    def count(self, css: str) -> int: ...

    @abstractmethod
    def text(self, css: str) -> str: ...

    @abstractmethod
    def attribute(self, css: str, attr: str) -> str: ...

    @abstractmethod
    def is_nth_enabled(self, css: str, index: int) -> bool: ...

    @abstractmethod
    def is_nth_selected(self, css: str, index: int) -> bool: ...

    @abstractmethod
    def nth_attribute(self, css: str, index: int, attr: str) -> str: ...

    @abstractmethod
    def nth_text(self, css: str, index: int) -> str: ...

    @abstractmethod
    def is_selected_within(self, parent_css: str, parent_index: int, child_css: str) -> bool: ...

    @abstractmethod
    def attribute_within(
        self, parent_css: str, parent_index: int, child_css: str, attr: str
    ) -> str: ...

    @abstractmethod
    def click(self, css: str) -> None: ...

    @abstractmethod
    def click_nth(self, css: str, index: int) -> None: ...

    @abstractmethod
    def click_xpath(self, xpath: str) -> None: ...

    @abstractmethod
    def send_keys(self, css: str, text: str, submit_after: bool) -> None: ...

    @abstractmethod
    def set_react_input_value(self, css: str, value: str) -> None: ...

    @abstractmethod
    def extract_all_via_script(self, script: str) -> list[dict[str, str]]: ...

    @abstractmethod
    def execute_script(self, script: str, *args: Any) -> Any: ...

    @abstractmethod
    def wait_until_visible(self, css: str) -> None: ...

    @abstractmethod
    def wait_until_present(self, css: str) -> None: ...

    @abstractmethod
    def wait_until_count_more_than(self, css: str, count: int) -> None: ...

    @abstractmethod
    def wait_until_url_contains(self, fragment: str) -> None: ...

    @abstractmethod
    def wait_until_url_matches(self, regex: str) -> None: ...

    @abstractmethod
    def wait_until_attribute_changes(
        self, css: str, index: int, attr: str, previous_value: str
    ) -> None: ...

    @abstractmethod
    def wait_until_any_present(self, *css_list: str) -> None: ...

    @abstractmethod
    def wait_until_condition(
        self, condition: Callable[[], bool], timeout_seconds: int
    ) -> None: ...

    @abstractmethod
    def try_wait_until_present(self, css: str, timeout_seconds: int) -> bool: ...

    @abstractmethod
    def close(self) -> None: ...
