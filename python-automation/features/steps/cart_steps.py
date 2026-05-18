import re

from behave import given, then, when

from automation.dsl.protocols.driver_factory import create_cart


def _cart():
    return create_cart()


@given("the shopper notes the current cart count")
def step_shopper_notes_cart_count(context):
    context.noted_item_count = _cart().get_cart_state().item_count


@when("the shopper adds the product to the cart")
def step_shopper_adds_product_to_cart(context):
    _cart().add_product_to_cart()


@when("the shopper navigates to the cart page")
def step_shopper_navigates_to_cart_page(context):
    _cart().view_cart()


@when("the shopper returns to the homepage")
def step_shopper_returns_to_homepage(context):
    _cart().browse_catalogue()


@when("the shopper changes the quantity to {quantity:d}")
def step_shopper_changes_quantity_to(context, quantity):
    _cart().change_quantity_to(quantity)


@when("the shopper removes the first cart item")
def step_shopper_removes_first_cart_item(context):
    _cart().remove_first_item_from_cart()


@when("the shopper notes the current cart total")
def step_shopper_notes_cart_total(context):
    context.noted_total = _cart().get_cart_state().total


@then("the cart badge should show {expected_count:d} item")
@then("the cart badge should show {expected_count:d} items")
def step_cart_badge_should_show(context, expected_count):
    actual = _cart().get_cart_state().item_count
    assert actual == expected_count, (
        f"cart badge should show {expected_count} item(s), got {actual}"
    )


@then("the cart badge should have increased by {increment:d}")
def step_cart_badge_increased_by(context, increment):
    expected = context.noted_item_count + increment
    actual = _cart().get_cart_state().item_count
    assert actual == expected, (
        f"cart badge should have increased by {increment}; "
        f"expected {expected}, got {actual}"
    )


@then("the cart should contain at least {minimum:d} items")
def step_cart_should_contain_at_least(context, minimum):
    actual = len(_cart().get_cart_state().items)
    assert actual >= minimum, (
        f"cart should contain at least {minimum} items, got {actual}"
    )


@then("the cart total should be visible and show a price")
def step_cart_total_visible_and_shows_price(context):
    total = _cart().get_cart_state().total
    assert re.search(r"\$\d+", total), (
        f"cart total should show a price with $, got: {total!r}"
    )


@then("the cart total should have changed")
def step_cart_total_should_have_changed(context):
    current = _cart().get_cart_state().total
    assert current != context.noted_total, (
        f"cart total should have changed from {context.noted_total!r}, still {current!r}"
    )


@then("the cart should show an empty state")
def step_cart_should_show_empty_state(context):
    assert _cart().get_cart_state().is_empty, "cart should show empty state"


@then("the first cart item should be visible")
def step_first_cart_item_visible(context):
    items = _cart().get_cart_state().items
    assert items, "cart should have at least one item"
