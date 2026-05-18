from automation.driver.api.http_holder import get_http
from automation.driver.ports.http_port import HttpPort
from automation.dsl.domain import (
    DeliveryOption,
    DeliveryState,
    ProductCard,
    ProductDetail,
    ProductListing,
    SearchResults,
)
from automation.dsl.protocols import driver_registry
from automation.dsl.protocols.catalogue_protocol import CatalogueProtocol
from automation.dsl.protocols.channel import Channel

BASE_URL = "http://localhost:8001"


class ApiMyEcommerceDriver(CatalogueProtocol):

    def __init__(self, http: HttpPort) -> None:
        self._http = http
        self._current_product_id = -1
        self._last_search_term = ""

    def browse_catalogue(self) -> None:
        return None

    def return_to_product_listing(self) -> None:
        return None

    def choose_alternative_delivery_option(self) -> None:
        return None

    def view_first_product(self) -> None:
        products = self._fetch_products("")
        if not products:
            raise RuntimeError("No products available to view")
        self._current_product_id = int(products[0]["id"])

    def search_for(self, term: str) -> None:
        self._last_search_term = term

    def get_product_listing(self) -> ProductListing:
        cards = [self._to_card(p) for p in self._fetch_products("")]
        return ProductListing(cards=cards, has_visible_loading_indicators=False)

    def get_product_detail(self) -> ProductDetail:
        product = self._fetch_product_detail(self._current_product_id)
        image_url = product.get("imageUrl") or ""
        return ProductDetail(
            title=product.get("title", ""),
            price=self._format_price(product.get("price", 0)),
            description=product.get("description", ""),
            image_present=bool(image_url.strip()),
            add_to_cart_button_text="Add to Cart",
            add_to_cart_enabled=True,
        )

    def get_delivery_state(self) -> DeliveryState:
        active = self._fetch_active_delivery_options()
        if not active:
            return DeliveryState(
                section_visible=False,
                options=[],
                header_text="",
                minimum_order_text_present=False,
            )
        options = [
            DeliveryOption(label=opt.get("name", ""), selected=(i == 0))
            for i, opt in enumerate(active)
        ]
        return DeliveryState(
            section_visible=True,
            options=options,
            header_text="Delivery Options",
            minimum_order_text_present=False,
        )

    def get_search_results(self) -> SearchResults:
        cards = [self._to_card(p) for p in self._fetch_products(self._last_search_term)]
        return SearchResults(cards=cards, empty_state_visible=not cards)

    def current_url(self) -> str:
        if self._last_search_term.strip():
            return f"{BASE_URL}/products?search={self._last_search_term}"
        if self._current_product_id >= 0:
            return f"{BASE_URL}/products/{self._current_product_id}"
        return f"{BASE_URL}/products"

    def _fetch_products(self, search_term: str) -> list[dict]:
        if not search_term.strip():
            return self._http.get_list_as("/products")
        return self._http.get_list_with_query_as("/products", "search", search_term)

    def _fetch_product_detail(self, product_id: int) -> dict:
        return self._http.get_as(f"/products/{product_id}")

    def _fetch_active_delivery_options(self) -> list[dict]:
        product = self._fetch_product_detail(self._current_product_id)
        delivery_options = product.get("deliveryOptions") or []
        return [opt for opt in delivery_options if opt.get("isActive")]

    def _to_card(self, p: dict) -> ProductCard:
        return ProductCard(
            title=p.get("title", ""),
            price=self._format_price(p.get("price", 0)),
            image_url=self._absolute_image_url(p.get("imageUrl") or ""),
        )

    @staticmethod
    def _format_price(price: float | int) -> str:
        return f"${price:.2f}"

    @staticmethod
    def _absolute_image_url(image_url: str) -> str:
        if not image_url.strip():
            return ""
        if image_url.startswith("http"):
            return image_url
        return f"{BASE_URL}{image_url}"


def _build_driver() -> ApiMyEcommerceDriver:
    return ApiMyEcommerceDriver(get_http())


driver_registry.register(Channel.API, _build_driver)
