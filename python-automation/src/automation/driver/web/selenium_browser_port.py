import re
from collections.abc import Callable
from typing import Any

from selenium.common.exceptions import TimeoutException, WebDriverException
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from automation.driver.ports.browser_port import BrowserPort

_DEFAULT_TIMEOUT_SECONDS = 10


class SeleniumBrowserPort(BrowserPort):

    def __init__(self, driver: WebDriver) -> None:
        self._driver = driver

    def navigate_to(self, url: str) -> None:
        self._driver.get(url)

    def navigate_back(self) -> None:
        self._driver.back()

    def current_url(self) -> str:
        return self._driver.current_url

    def is_visible(self, css: str) -> bool:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        if not els:
            return False
        try:
            return els[0].is_displayed()
        except WebDriverException:
            return False

    def is_present(self, css: str) -> bool:
        return len(self._driver.find_elements(By.CSS_SELECTOR, css)) > 0

    def is_enabled(self, css: str) -> bool:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return bool(els) and els[0].is_enabled()

    def is_selected(self, css: str) -> bool:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return bool(els) and els[0].is_selected()

    def count(self, css: str) -> int:
        return len(self._driver.find_elements(By.CSS_SELECTOR, css))

    def text(self, css: str) -> str:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return els[0].text.strip() if els else ""

    def attribute(self, css: str, attr: str) -> str:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        if not els:
            return ""
        return els[0].get_attribute(attr) or ""

    def is_nth_enabled(self, css: str, index: int) -> bool:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return index < len(els) and els[index].is_enabled()

    def is_nth_selected(self, css: str, index: int) -> bool:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return index < len(els) and els[index].is_selected()

    def nth_attribute(self, css: str, index: int, attr: str) -> str:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        if index >= len(els):
            return ""
        return els[index].get_attribute(attr) or ""

    def nth_text(self, css: str, index: int) -> str:
        els = self._driver.find_elements(By.CSS_SELECTOR, css)
        return els[index].text.strip() if index < len(els) else ""

    def is_selected_within(self, parent_css: str, parent_index: int, child_css: str) -> bool:
        parents = self._driver.find_elements(By.CSS_SELECTOR, parent_css)
        if parent_index >= len(parents):
            return False
        children = parents[parent_index].find_elements(By.CSS_SELECTOR, child_css)
        return bool(children) and children[0].is_selected()

    def attribute_within(
        self, parent_css: str, parent_index: int, child_css: str, attr: str
    ) -> str:
        parents = self._driver.find_elements(By.CSS_SELECTOR, parent_css)
        if parent_index >= len(parents):
            return ""
        children = parents[parent_index].find_elements(By.CSS_SELECTOR, child_css)
        if not children:
            return ""
        return children[0].get_attribute(attr) or ""

    def click(self, css: str) -> None:
        self._driver.find_element(By.CSS_SELECTOR, css).click()

    def click_nth(self, css: str, index: int) -> None:
        self._driver.find_elements(By.CSS_SELECTOR, css)[index].click()

    def click_xpath(self, xpath: str) -> None:
        self._driver.find_element(By.XPATH, xpath).click()

    def send_keys(self, css: str, text: str, submit_after: bool) -> None:
        el = self._driver.find_element(By.CSS_SELECTOR, css)
        el.clear()
        el.send_keys(text)
        if submit_after:
            el.send_keys(Keys.ENTER)

    def set_react_input_value(self, css: str, value: str) -> None:
        input_el = self._driver.find_element(By.CSS_SELECTOR, css)
        self._driver.execute_script(
            "var setter = Object.getOwnPropertyDescriptor("
            "window.HTMLInputElement.prototype, 'value').set;"
            "setter.call(arguments[0], arguments[1]);"
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            input_el,
            value,
        )

    def extract_all_via_script(self, script: str) -> list[dict[str, str]]:
        result = self._driver.execute_script(script)
        return result if isinstance(result, list) else []

    def execute_script(self, script: str, *args: Any) -> Any:
        return self._driver.execute_script(script, *args)

    def wait_until_visible(self, css: str) -> None:
        self._wait().until(EC.visibility_of_element_located((By.CSS_SELECTOR, css)))

    def wait_until_present(self, css: str) -> None:
        self._wait().until(EC.presence_of_element_located((By.CSS_SELECTOR, css)))

    def wait_until_count_more_than(self, css: str, count: int) -> None:
        self._wait().until(
            lambda d: len(d.find_elements(By.CSS_SELECTOR, css)) > count
        )

    def wait_until_url_contains(self, fragment: str) -> None:
        self._wait().until(EC.url_contains(fragment))

    def wait_until_url_matches(self, regex: str) -> None:
        compiled = re.compile(regex)
        self._wait().until(lambda d: bool(compiled.search(d.current_url)))

    def wait_until_attribute_changes(
        self, css: str, index: int, attr: str, previous_value: str
    ) -> None:
        def _changed(d: WebDriver) -> bool:
            els = d.find_elements(By.CSS_SELECTOR, css)
            if index >= len(els):
                return False
            return els[index].get_attribute(attr) != previous_value

        self._wait(5).until(_changed)

    def wait_until_any_present(self, *css_list: str) -> None:
        self._wait().until(
            lambda d: any(d.find_elements(By.CSS_SELECTOR, c) for c in css_list)
        )

    def wait_until_condition(
        self, condition: Callable[[], bool], timeout_seconds: int
    ) -> None:
        self._wait(timeout_seconds).until(lambda _d: condition())

    def try_wait_until_present(self, css: str, timeout_seconds: int) -> bool:
        try:
            self._wait(timeout_seconds).until(
                lambda d: bool(d.find_elements(By.CSS_SELECTOR, css))
            )
            return True
        except TimeoutException:
            return False

    def close(self) -> None:
        try:
            self._driver.quit()
        except Exception:
            pass

    def _wait(self, seconds: int = _DEFAULT_TIMEOUT_SECONDS) -> WebDriverWait:
        return WebDriverWait(self._driver, seconds)
