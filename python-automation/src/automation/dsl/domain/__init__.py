from dataclasses import dataclass, field


@dataclass(frozen=True)
class ProductCard:
    title: str
    price: str
    image_url: str


@dataclass(frozen=True)
class ProductListing:
    cards: list[ProductCard]
    has_visible_loading_indicators: bool


@dataclass(frozen=True)
class ProductDetail:
    title: str
    price: str
    description: str
    image_present: bool
    add_to_cart_button_text: str
    add_to_cart_enabled: bool


@dataclass(frozen=True)
class DeliveryOption:
    label: str
    selected: bool


@dataclass(frozen=True)
class DeliveryState:
    section_visible: bool
    options: list[DeliveryOption]
    header_text: str
    minimum_order_text_present: bool

    def selected_option_count(self) -> int:
        return sum(1 for o in self.options if o.selected)


@dataclass(frozen=True)
class SearchResults:
    cards: list[ProductCard]
    empty_state_visible: bool


@dataclass(frozen=True)
class CartItem:
    title: str


@dataclass(frozen=True)
class CartState:
    item_count: int
    total: str
    items: list[CartItem] = field(default_factory=list)
    is_empty: bool = True

    def has_items(self) -> bool:
        return self.item_count > 0


@dataclass(frozen=True)
class SavedState:
    save_button_present: bool
    save_button_pressed: bool
    save_button_enabled: bool
    saved_page_count: int
    wishlist_link_visible: bool
