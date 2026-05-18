import json
import re
import time
from collections.abc import Callable
from typing import Any

from playwright.sync_api import Page, TimeoutError as PlaywrightTimeoutError

from automation.driver.ports.browser_port import BrowserPort

_DEFAULT_TIMEOUT_MS = 10_000


class PlaywrightBrowserPort(BrowserPort):

    def __init__(self, page: Page) -> None:
        self._page = page

    def navigate_to(self, url: str) -> None:
        self._page.goto(url)

    def navigate_back(self) -> None:
        self._page.go_back()

    def current_url(self) -> str:
        return self._page.url

    def is_visible(self, css: str) -> bool:
        try:
            return self._page.locator(css).first.is_visible()
        except PlaywrightTimeoutError:
            return False

    def is_present(self, css: str) -> bool:
        return self._page.locator(css).count() > 0

    def is_enabled(self, css: str) -> bool:
        try:
            return self._page.locator(css).first.is_enabled()
        except PlaywrightTimeoutError:
            return False

    def is_selected(self, css: str) -> bool:
        try:
            return self._page.locator(css).first.is_checked()
        except PlaywrightTimeoutError:
            return False

    def count(self, css: str) -> int:
        return self._page.locator(css).count()

    def text(self, css: str) -> str:
        loc = self._page.locator(css).first
        if loc.count() == 0:
            return ""
        return (loc.text_content() or "").strip()

    def attribute(self, css: str, attr: str) -> str:
        loc = self._page.locator(css).first
        if loc.count() == 0:
            return ""
        return loc.get_attribute(attr) or ""

    def is_nth_enabled(self, css: str, index: int) -> bool:
        loc = self._page.locator(css).nth(index)
        return loc.count() > 0 and loc.is_enabled()

    def is_nth_selected(self, css: str, index: int) -> bool:
        loc = self._page.locator(css).nth(index)
        if loc.count() == 0:
            return False
        try:
            return loc.is_checked()
        except PlaywrightTimeoutError:
            return False

    def nth_attribute(self, css: str, index: int, attr: str) -> str:
        loc = self._page.locator(css).nth(index)
        if loc.count() == 0:
            return ""
        return loc.get_attribute(attr) or ""

    def nth_text(self, css: str, index: int) -> str:
        loc = self._page.locator(css).nth(index)
        if loc.count() == 0:
            return ""
        return (loc.text_content() or "").strip()

    def is_selected_within(self, parent_css: str, parent_index: int, child_css: str) -> bool:
        parent = self._page.locator(parent_css).nth(parent_index)
        if parent.count() == 0:
            return False
        child = parent.locator(child_css).first
        if child.count() == 0:
            return False
        try:
            return child.is_checked()
        except PlaywrightTimeoutError:
            return False

    def attribute_within(
        self, parent_css: str, parent_index: int, child_css: str, attr: str
    ) -> str:
        parent = self._page.locator(parent_css).nth(parent_index)
        if parent.count() == 0:
            return ""
        child = parent.locator(child_css).first
        if child.count() == 0:
            return ""
        return child.get_attribute(attr) or ""

    def click(self, css: str) -> None:
        self._page.locator(css).first.click()

    def click_nth(self, css: str, index: int) -> None:
        self._page.locator(css).nth(index).click()

    def click_xpath(self, xpath: str) -> None:
        self._page.locator(f"xpath={xpath}").first.click()

    def send_keys(self, css: str, text: str, submit_after: bool) -> None:
        loc = self._page.locator(css).first
        loc.clear()
        loc.fill(text)
        if submit_after:
            loc.press("Enter")

    def set_react_input_value(self, css: str, value: str) -> None:
        self._page.evaluate(
            """({selector, val}) => {
                const input = document.querySelector(selector);
                if (!input) return;
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(input, val);
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
            }""",
            {"selector": css, "val": value},
        )

    def extract_all_via_script(self, script: str) -> list[dict[str, str]]:
        result = self._page.evaluate(f"(() => {{ {script} }})()")
        return result if isinstance(result, list) else []

    def execute_script(self, script: str, *args: Any) -> Any:
        encoded = json.dumps(list(args))
        return self._page.evaluate(f"((args) => {{ {script} }})({encoded})")

    def wait_until_visible(self, css: str) -> None:
        self._page.locator(css).first.wait_for(state="visible", timeout=_DEFAULT_TIMEOUT_MS)

    def wait_until_present(self, css: str) -> None:
        self._page.locator(css).first.wait_for(state="attached", timeout=_DEFAULT_TIMEOUT_MS)

    def wait_until_count_more_than(self, css: str, count: int) -> None:
        self._page.wait_for_function(
            "({sel, min}) => document.querySelectorAll(sel).length > min",
            arg={"sel": css, "min": count},
            timeout=_DEFAULT_TIMEOUT_MS,
        )

    def wait_until_url_contains(self, fragment: str) -> None:
        self._page.wait_for_url(lambda url: fragment in url, timeout=_DEFAULT_TIMEOUT_MS)

    def wait_until_url_matches(self, regex: str) -> None:
        self._page.wait_for_url(re.compile(regex), timeout=_DEFAULT_TIMEOUT_MS)

    def wait_until_attribute_changes(
        self, css: str, index: int, attr: str, previous_value: str
    ) -> None:
        self._page.wait_for_function(
            "({sel, idx, attribute, prev}) => { "
            "const els = document.querySelectorAll(sel); "
            "const el = els[idx]; "
            "return el !== undefined && el.getAttribute(attribute) !== prev; }",
            arg={"sel": css, "idx": index, "attribute": attr, "prev": previous_value},
            timeout=5_000,
        )

    def wait_until_any_present(self, *css_list: str) -> None:
        deadline = time.time() + _DEFAULT_TIMEOUT_MS / 1000
        while time.time() < deadline:
            for css in css_list:
                if self.is_present(css):
                    return
            self._page.wait_for_timeout(100)
        raise PlaywrightTimeoutError(f"None of {css_list} appeared within timeout")

    def wait_until_condition(
        self, condition: Callable[[], bool], timeout_seconds: int
    ) -> None:
        deadline = time.time() + timeout_seconds
        while time.time() < deadline:
            if condition():
                return
            self._page.wait_for_timeout(200)
        raise PlaywrightTimeoutError(f"Condition not met within {timeout_seconds}s")

    def try_wait_until_present(self, css: str, timeout_seconds: int) -> bool:
        try:
            self._page.locator(css).first.wait_for(
                state="attached", timeout=timeout_seconds * 1000
            )
            return True
        except PlaywrightTimeoutError:
            return False

    def close(self) -> None:
        try:
            self._page.context.browser.close()  # type: ignore[union-attr]
        except Exception:
            pass
