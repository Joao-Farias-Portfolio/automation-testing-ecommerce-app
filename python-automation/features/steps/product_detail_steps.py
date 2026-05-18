import re

from behave import given, then, when

from automation.dsl.protocols.driver_factory import create_cart, create_catalogue


def _catalogue():
    return create_catalogue()


def _cart():
    return create_cart()


@given("the shopper is viewing the first product")
def step_shopper_viewing_first_product(context):
    cat = _catalogue()
    cat.browse_catalogue()
    context.captured_listing_title = cat.get_product_listing().cards[0].title
    cat.view_first_product()


@given("the shopper notes the cart count on the detail page")
def step_shopper_notes_cart_count_on_detail_page(context):
    context.noted_cart_count = _cart().get_cart_state().item_count


@when("the shopper clicks the first product card")
def step_shopper_clicks_first_product_card(context):
    _catalogue().view_first_product()


@when("the shopper adds the product to the cart from the detail page")
def step_shopper_adds_product_from_detail_page(context):
    _cart().add_product_to_cart()


@when("the shopper navigates back")
def step_shopper_navigates_back(context):
    _catalogue().return_to_product_listing()


@then("the URL should match the product detail pattern")
def step_url_matches_product_detail(context):
    url = _catalogue().current_url()
    assert re.match(r".*/products/\d+", url), (
        f"URL should match product detail pattern, got {url!r}"
    )


@then("the product title should be visible on the detail page")
def step_product_title_visible_on_detail_page(context):
    title = _catalogue().get_product_detail().title
    assert title.strip(), "product title should be visible and non-blank"


@then("the product detail page should show price, description and image")
def step_detail_page_shows_price_description_and_image(context):
    detail = _catalogue().get_product_detail()
    assert re.search(r"\$\d+", detail.price), (
        f"price should show a $ value, got {detail.price!r}"
    )
    assert detail.description.strip(), "description should not be blank"
    assert detail.image_present, "product image should be present"


@then("the product title should match the one from the listing")
def step_product_title_matches_listing(context):
    detail_title = _catalogue().get_product_detail().title
    listing_title = context.captured_listing_title.strip()
    assert listing_title in detail_title, (
        f"detail title {detail_title!r} should contain listing title {listing_title!r}"
    )


@then("the add to cart button should show Added to Cart and be disabled")
def step_add_to_cart_button_added_and_disabled(context):
    detail = _catalogue().get_product_detail()
    assert "added to cart" in detail.add_to_cart_button_text.lower(), (
        f"button text should say 'Added to Cart', got {detail.add_to_cart_button_text!r}"
    )
    assert not detail.add_to_cart_enabled, "add-to-cart button should be disabled"


@then("the shopper should be back on the product listing")
def step_shopper_back_on_product_listing(context):
    cat = _catalogue()
    cards = cat.get_product_listing().cards
    assert cards, "product listing should be visible after navigating back"
    url = cat.current_url()
    assert re.match(r".*/(\?.*)?$", url), (
        f"URL should be back on the listing, got {url!r}"
    )
