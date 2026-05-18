import os
from typing import Any

from automation.driver.api import http_holder
from automation.driver.api.httpx_http_port import HttpxHttpPort
from automation.driver.api.requests_http_port import RequestsHttpPort
from automation.driver.web import browser_holder
from automation.driver.web.playwright_browser_port import PlaywrightBrowserPort
from automation.driver.web.selenium_browser_port import SeleniumBrowserPort
from automation.dsl.protocols.channel import Channel, current_channel

WEB_BASE_URL = "http://localhost:3001"
API_BASE_URL = "http://localhost:8001"


def before_all(context: Any) -> None:
    context.channel = current_channel()
    if context.channel is Channel.WEB:
        _start_web(context)
    else:
        _start_api(context)


def before_scenario(context: Any, scenario: Any) -> None:
    if context.channel is Channel.WEB:
        _clear_browser_storage(context)
    # API channel: each scenario gets a fresh driver instance via the registry,
    # but the underlying HTTP client is shared.


def after_scenario(context: Any, scenario: Any) -> None:
    if context.channel is Channel.WEB and getattr(context, "browser_impl", None) == "playwright":
        # Playwright: open a fresh page so the next scenario starts clean
        page = context.playwright_page
        try:
            page.goto("about:blank")
        except Exception:
            pass


def after_all(context: Any) -> None:
    if context.channel is Channel.WEB:
        _stop_web(context)
    else:
        _stop_api(context)


def _start_web(context: Any) -> None:
    impl = os.environ.get("BROWSER_IMPL", "playwright").lower()
    headed = os.environ.get("HEADED", "false").lower() == "true"
    context.browser_impl = impl
    if impl == "selenium":
        from selenium import webdriver
        from selenium.webdriver.chrome.options import Options

        options = Options()
        if not headed:
            options.add_argument("--headless=new")
            options.add_argument("--no-sandbox")
            options.add_argument("--disable-dev-shm-usage")
            options.add_argument("--window-size=1920,1080")
            options.add_argument("--disable-extensions")
        else:
            options.add_argument("--window-size=1920,1080")
            options.add_argument("--disable-extensions")
        context.selenium_driver = webdriver.Chrome(options=options)
        port = SeleniumBrowserPort(context.selenium_driver)
    elif impl == "playwright":
        from playwright.sync_api import sync_playwright

        context.playwright = sync_playwright().start()
        context.playwright_browser = context.playwright.chromium.launch(headless=not headed)
        context.playwright_page = context.playwright_browser.new_page()
        port = PlaywrightBrowserPort(context.playwright_page)
    else:
        raise RuntimeError(
            f"Unknown BROWSER_IMPL '{impl}'. Valid values: playwright, selenium"
        )
    browser_holder.set_browser(port)
    # Import after the holder is populated so the registration sees a live browser.
    import automation.driver.web.my_ecommerce_driver  # noqa: F401


def _stop_web(context: Any) -> None:
    impl = getattr(context, "browser_impl", "playwright")
    if impl == "playwright":
        try:
            if hasattr(context, "playwright_page"):
                context.playwright_page.close()
        except Exception:
            pass
        try:
            if hasattr(context, "playwright_browser"):
                context.playwright_browser.close()
        except Exception:
            pass
        try:
            if hasattr(context, "playwright"):
                context.playwright.stop()
        except Exception:
            pass
    else:
        try:
            if hasattr(context, "selenium_driver"):
                context.selenium_driver.quit()
        except Exception:
            pass
    browser_holder.clear_browser()


def _clear_browser_storage(context: Any) -> None:
    impl = context.browser_impl
    script = (
        "try {"
        "  localStorage.removeItem('cart');"
        "  localStorage.removeItem('cartItems');"
        "  localStorage.removeItem('selectedDelivery');"
        "} catch(e) {}"
    )
    try:
        if impl == "playwright":
            page = context.playwright_page
            page.goto(WEB_BASE_URL)
            page.evaluate(f"() => {{ {script} }}")
        else:
            driver = context.selenium_driver
            driver.get(WEB_BASE_URL)
            driver.execute_script(script)
    except Exception:
        pass


def _start_api(context: Any) -> None:
    impl = os.environ.get("HTTP_IMPL", "httpx").lower()
    context.http_impl = impl
    if impl == "httpx":
        port = HttpxHttpPort(API_BASE_URL)
    elif impl == "requests":
        port = RequestsHttpPort(API_BASE_URL)
    else:
        raise RuntimeError(
            f"Unknown HTTP_IMPL '{impl}'. Valid values: httpx, requests"
        )
    http_holder.set_http(port)
    import automation.driver.api.my_ecommerce_driver  # noqa: F401


def _stop_api(context: Any) -> None:
    http_holder.clear_http()
