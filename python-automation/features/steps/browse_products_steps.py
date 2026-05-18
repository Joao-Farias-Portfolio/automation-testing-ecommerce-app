import re

from behave import given, then

from automation.dsl.protocols.driver_factory import create_catalogue


def _catalogue():
    return create_catalogue()


@given("the shopper is on the homepage")
def step_shopper_is_on_homepage(context):
    _catalogue().browse_catalogue()


@given("the homepage has loaded with products")
def step_homepage_has_loaded_with_products(context):
    _catalogue().browse_catalogue()


@given("the shopper is on the homepage with products loaded")
def step_shopper_on_homepage_with_products_loaded(context):
    _catalogue().browse_catalogue()


@then("product cards should be visible")
def step_product_cards_should_be_visible(context):
    cards = _catalogue().get_product_listing().cards
    assert cards, "product cards should be visible on the page"


@then("each product card should show a title and price")
def step_each_card_shows_title_and_price(context):
    cards = _catalogue().get_product_listing().cards
    assert cards, "expected at least one product card"
    first = cards[0]
    assert first.title.strip(), "product title should be visible on first card"
    assert first.price.strip(), "product price should be visible on first card"


@then("the page should show a loading indicator briefly")
def step_page_shows_loading_indicator_briefly(context):
    listing = _catalogue().get_product_listing()
    assert listing.cards, "product cards should be visible after loading completes"
    assert not listing.has_visible_loading_indicators, (
        "loading indicators should be gone once product cards are visible"
    )


@then("product images should have valid sources")
def step_product_images_should_have_valid_sources(context):
    cards = _catalogue().get_product_listing().cards
    pattern = re.compile(r"https?://.+")
    for card in cards:
        assert pattern.match(card.image_url), (
            f"product image URL should be a valid http(s) URL, got: {card.image_url!r}"
        )
