from automation.driver.ports.browser_port import BrowserPort
from automation.driver.web.browser_holder import get_browser
from automation.dsl.domain import (
    CartItem,
    CartState,
    DeliveryOption,
    DeliveryState,
    ProductCard,
    ProductDetail,
    ProductListing,
    SavedState,
    SearchResults,
)
from automation.dsl.protocols import driver_registry
from automation.dsl.protocols.channel import Channel
from automation.dsl.protocols.my_ecommerce_protocol import MyEcommerceProtocol

BASE_URL = "http://localhost:3001"

_DELIVERY_SELECTOR = (
    "[data-testid='delivery-section'], [data-testid='delivery-options'], "
    "[data-testid='shipping-section'], [data-testid='shipping-options']"
)


class WebMyEcommerceDriver(MyEcommerceProtocol):

    def __init__(self, browser: BrowserPort) -> None:
        self._browser = browser

    def browse_catalogue(self) -> None:
        self._browser.navigate_to(BASE_URL)
        self._browser.wait_until_visible("[data-testid='product-card']")

    def view_cart(self) -> None:
        self._browser.navigate_to(f"{BASE_URL}/cart")
        self._browser.wait_until_any_present(
            "[data-testid='cart-item']", "[data-testid='empty-cart']"
        )

    def view_saved_items(self) -> None:
        self._browser.navigate_to(f"{BASE_URL}/saved")
        self._browser.wait_until_url_contains("/saved")

    def return_to_product_listing(self) -> None:
        self._browser.navigate_back()

    def add_product_to_cart(self) -> None:
        self._browser.wait_until_count_more_than("[data-testid='add-to-cart']", 0)
        count_before = self._read_cart_count()
        button_count = self._browser.count("[data-testid='add-to-cart']")
        enabled_index = next(
            (
                i
                for i in range(button_count)
                if self._browser.is_nth_enabled("[data-testid='add-to-cart']", i)
            ),
            0,
        )
        self._browser.click_nth("[data-testid='add-to-cart']", enabled_index)
        self._browser.wait_until_condition(
            lambda: self._read_cart_count() > count_before, 10
        )

    def remove_first_item_from_cart(self) -> None:
        self._browser.wait_until_count_more_than("[data-testid='remove-item']", 0)
        count_before = self._read_cart_count()
        self._browser.click_nth("[data-testid='remove-item']", 0)
        self._browser.wait_until_condition(
            lambda: self._read_cart_count() < count_before
            or self._browser.is_present("[data-testid='empty-cart']"),
            10,
        )

    def change_quantity_to(self, quantity: int) -> None:
        self._browser.wait_until_present("[data-testid='quantity-display']")
        total_before = self._read_cart_total()
        self._browser.set_react_input_value(
            "[data-testid='quantity-display']", str(quantity)
        )
        self._browser.wait_until_condition(
            lambda: self._read_cart_total() != total_before, 10
        )

    def search_for(self, term: str) -> None:
        self._browser.send_keys("input[placeholder*='Search Items']", term, True)
        self._browser.wait_until_url_contains("/search/")
        self._browser.wait_until_condition(
            lambda: self._browser.count("[data-testid='product-card']") > 0
            or "No products found" in self._browser.text("body"),
            10,
        )

    def view_first_product(self) -> None:
        self._browser.wait_until_visible("[data-testid='product-card']")
        self._browser.click_nth("[data-testid='product-card']", 0)
        self._browser.wait_until_url_matches(r".*/products/\d+")

    def choose_alternative_delivery_option(self) -> None:
        radio_count = self._browser.count("input[type='radio']")
        for i in range(radio_count):
            if not self._browser.is_nth_selected("input[type='radio']", i):
                self._browser.click_xpath(f"(//input[@type='radio'])[{i + 1}]/../../..")
                return

    def ensure_first_product_is_saved(self) -> None:
        self._browser.wait_until_visible("[data-testid='save-button']")
        pressed = (
            self._browser.attribute("[data-testid='save-button']", "aria-pressed") == "true"
        )
        if not pressed:
            self.toggle_save_state_of_first_product()

    def toggle_save_state_of_first_product(self) -> None:
        previous_state = self._browser.attribute("[data-testid='save-button']", "aria-pressed")
        self._browser.click_nth("[data-testid='save-button']", 0)
        self._browser.wait_until_attribute_changes(
            "[data-testid='save-button']", 0, "aria-pressed", previous_state
        )

    def view_wishlist(self) -> None:
        self._browser.click("[data-testid='wishlist-link']")
        self._browser.wait_until_url_contains("/saved")

    def get_product_listing(self) -> ProductListing:
        self._browser.wait_until_visible("[data-testid='product-card']")
        cards = self._extract_product_cards()
        loading_visible = self._browser.is_present(
            "[data-testid='loading']"
        ) and self._browser.is_visible("[data-testid='loading']")
        return ProductListing(cards=cards, has_visible_loading_indicators=loading_visible)

    def get_cart_state(self) -> CartState:
        item_count = self._read_cart_count()
        total = self._read_cart_total()
        items = self._read_cart_items()
        is_empty = self._browser.is_present("[data-testid='empty-cart']")
        return CartState(item_count=item_count, total=total, items=items, is_empty=is_empty)

    def get_product_detail(self) -> ProductDetail:
        self._browser.wait_until_visible("[data-testid='product-title']")
        return ProductDetail(
            title=self._browser.text("[data-testid='product-title']"),
            price=self._browser.text("[data-testid='product-price']"),
            description=self._browser.text("[data-testid='product-description']"),
            image_present=self._browser.is_present("[data-testid='product-detail-image']"),
            add_to_cart_button_text=self._browser.text("[data-testid='add-to-cart']"),
            add_to_cart_enabled=self._browser.is_enabled("[data-testid='add-to-cart']"),
        )

    def get_delivery_state(self) -> DeliveryState:
        self._browser.wait_until_visible("[data-testid='product-title']")
        found = self._browser.try_wait_until_present(_DELIVERY_SELECTOR, 5)
        if not found or not self._browser.is_visible(_DELIVERY_SELECTOR):
            return DeliveryState(
                section_visible=False,
                options=[],
                header_text="",
                minimum_order_text_present=False,
            )
        radio_count = self._browser.count("input[type='radio']")
        options: list[DeliveryOption] = []
        for i in range(radio_count):
            label = self._browser.nth_attribute("input[type='radio']", i, "value")
            selected = self._browser.is_nth_selected("input[type='radio']", i)
            options.append(DeliveryOption(label=label or str(i), selected=selected))
        header = self._browser.nth_text(f"{_DELIVERY_SELECTOR} p", 0)
        minimum_order = self._browser.is_present(
            "[data-testid='minimum-order'], [data-testid='min-order']"
        )
        return DeliveryState(
            section_visible=True,
            options=options,
            header_text=header,
            minimum_order_text_present=minimum_order,
        )

    def get_search_results(self) -> SearchResults:
        self._browser.wait_until_url_contains("/search/")
        cards = self._extract_product_cards()
        empty_state = self._browser.is_present("[data-testid='no-results']")
        return SearchResults(cards=cards, empty_state_visible=empty_state)

    def get_saved_state(self) -> SavedState:
        self._browser.wait_until_any_present(
            "[data-testid='product-card']",
            "[data-testid='save-button']",
            "[data-testid='wishlist-link']",
        )
        present = self._browser.is_present("[data-testid='save-button']")
        pressed = present and (
            self._browser.attribute("[data-testid='save-button']", "aria-pressed") == "true"
        )
        enabled = present and self._browser.is_enabled("[data-testid='save-button']")
        return SavedState(
            save_button_present=present,
            save_button_pressed=pressed,
            save_button_enabled=enabled,
            saved_page_count=self._read_saved_count(),
            wishlist_link_visible=self._browser.is_present("[data-testid='wishlist-link']"),
        )

    def current_url(self) -> str:
        return self._browser.current_url()

    def _extract_product_cards(self) -> list[ProductCard]:
        raw = self._browser.extract_all_via_script(
            "return Array.from(document.querySelectorAll('[data-testid=\"product-card\"]'))"
            ".map(card => ({"
            "title: (card.querySelector('[data-testid=\"product-title\"]')?.textContent ?? '').trim(),"
            "price: (card.querySelector('[data-testid=\"product-price\"]')?.textContent ?? '').trim(),"
            "imageUrl: card.querySelector('img')?.src ?? ''"
            "}));"
        )
        return [
            ProductCard(
                title=m.get("title", ""),
                price=m.get("price", ""),
                image_url=m.get("imageUrl", ""),
            )
            for m in raw
        ]

    def _read_cart_count(self) -> int:
        text = self._browser.text("[data-testid='cart-count']")
        return int(text) if text else 0

    def _read_cart_total(self) -> str:
        return self._browser.text("[data-testid='cart-total']")

    def _read_cart_items(self) -> list[CartItem]:
        n = self._browser.count("[data-testid='cart-item']")
        return [
            CartItem(title=self._browser.nth_text("[data-testid='cart-item']", i))
            for i in range(n)
        ]

    def _read_saved_count(self) -> int:
        text = self._browser.text("[data-testid='saved-count']")
        digits = "".join(c for c in text if c.isdigit())
        return int(digits) if digits else 0


def _build_driver() -> WebMyEcommerceDriver:
    return WebMyEcommerceDriver(get_browser())


driver_registry.register(Channel.WEB, _build_driver)
