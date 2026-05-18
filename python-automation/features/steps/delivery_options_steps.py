from behave import given, then, when

from automation.dsl.protocols.driver_factory import create_catalogue


def _catalogue():
    return create_catalogue()


@given("the shopper has navigated to a product detail page")
def step_shopper_navigated_to_product_detail(context):
    cat = _catalogue()
    cat.browse_catalogue()
    cat.view_first_product()


@when("the shopper notes the currently selected delivery option")
def step_shopper_notes_selected_delivery_option(context):
    selected = next(
        (o.label for o in _catalogue().get_delivery_state().options if o.selected),
        None,
    )
    assert selected is not None, "No delivery option is currently selected"
    context.noted_delivery_option = selected


@when("the shopper selects a different delivery option")
def step_shopper_selects_different_delivery_option(context):
    _catalogue().choose_alternative_delivery_option()


@then("the delivery section should be visible")
def step_delivery_section_visible(context):
    assert _catalogue().get_delivery_state().section_visible, (
        "delivery section should be visible on the product detail page"
    )


@then("the delivery section should contain radio button options")
def step_delivery_section_contains_radio_options(context):
    options = _catalogue().get_delivery_state().options
    assert options, "delivery section should contain radio button options"


@then("one delivery option should be selected by default")
def step_one_delivery_option_selected_by_default(context):
    state = _catalogue().get_delivery_state()
    assert state.section_visible, "delivery section must be visible"
    assert state.selected_option_count() == 1, (
        f"exactly one delivery option should be selected by default, "
        f"got {state.selected_option_count()}"
    )


@then("a different delivery option should now be selected")
def step_different_delivery_option_selected(context):
    state = _catalogue().get_delivery_state()
    assert state.section_visible, "delivery section must be visible"
    assert state.selected_option_count() == 1, (
        "exactly one delivery option should be selected after changing"
    )
    now_selected = next((o.label for o in state.options if o.selected), None)
    assert now_selected is not None, "No delivery option is selected after changing"
    assert now_selected != context.noted_delivery_option, (
        f"selected delivery option should have changed from {context.noted_delivery_option!r}"
    )


@then("no minimum order restrictions should be shown")
def step_no_minimum_order_restrictions(context):
    assert not _catalogue().get_delivery_state().minimum_order_text_present, (
        "no minimum order restrictions should be shown"
    )


@then("the delivery section should have a header with delivery options text")
def step_delivery_section_has_header(context):
    header = _catalogue().get_delivery_state().header_text
    assert header.strip(), "delivery section header should not be blank"


@then("the product detail page should still be functional without delivery options")
def step_product_detail_functional_without_delivery(context):
    detail = _catalogue().get_product_detail()
    assert detail.title.strip(), "product title should be present"
    assert detail.price.strip(), "product price should be present"
    assert detail.add_to_cart_button_text.strip(), "add-to-cart button should be present"
