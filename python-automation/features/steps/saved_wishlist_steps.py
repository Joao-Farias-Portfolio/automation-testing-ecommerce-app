from behave import given, then, when

from automation.dsl.protocols.driver_factory import create_saved


def _saved():
    return create_saved()


@given("the shopper is on the homepage with save buttons visible")
def step_shopper_on_homepage_with_save_buttons(context):
    _saved().browse_catalogue()


@given("the shopper records the initial save state of the first product")
def step_shopper_records_initial_save_state(context):
    context.initial_save_state = _saved().get_saved_state().save_button_pressed


@given("the shopper has saved the first product")
def step_shopper_has_saved_first_product(context):
    _saved().ensure_first_product_is_saved()


@given("the shopper is on the saved page")
def step_shopper_is_on_saved_page(context):
    _saved().view_saved_items()


@when("the shopper toggles the save button for the first product")
def step_shopper_toggles_save_button(context):
    _saved().toggle_save_state_of_first_product()


@when("the shopper toggles the save button again")
def step_shopper_toggles_save_button_again(context):
    _saved().toggle_save_state_of_first_product()


@when("the shopper navigates to the saved page")
def step_shopper_navigates_to_saved_page(context):
    _saved().view_saved_items()


@when("the shopper clicks the wishlist link")
def step_shopper_clicks_wishlist_link(context):
    _saved().view_wishlist()


@then("the save state of the first product should have changed")
def step_save_state_should_have_changed(context):
    current = _saved().get_saved_state().save_button_pressed
    assert current != context.initial_save_state, (
        f"save state should have changed from {context.initial_save_state}"
    )


@then("the save state should be restored to the initial state")
def step_save_state_should_be_restored(context):
    current = _saved().get_saved_state().save_button_pressed
    assert current == context.initial_save_state, (
        f"save state should be restored to {context.initial_save_state}, got {current}"
    )


@then("the saved count should be visible and show a number")
def step_saved_count_visible_and_shows_number(context):
    count = _saved().get_saved_state().saved_page_count
    assert count >= 1, f"saved count should be >= 1, got {count}"


@then("the wishlist link should be visible")
def step_wishlist_link_visible(context):
    assert _saved().get_saved_state().wishlist_link_visible, (
        "wishlist link should be visible on the saved page"
    )


@then("the URL should contain /saved")
def step_url_should_contain_saved(context):
    url = _saved().current_url()
    assert "/saved" in url, f"URL should contain '/saved', got {url!r}"


@then("the save button should be visible and functional on the detail page")
def step_save_button_visible_and_functional(context):
    state = _saved().get_saved_state()
    assert state.save_button_present, "save button should be present"
    assert state.save_button_enabled, "save button should be enabled"
