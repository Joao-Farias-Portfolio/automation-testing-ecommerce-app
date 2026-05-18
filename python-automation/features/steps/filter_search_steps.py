from behave import given, then, when

from automation.dsl.protocols.driver_factory import create_catalogue


def _catalogue():
    return create_catalogue()


@given("the shopper is on the homepage with products visible")
def step_shopper_on_homepage_with_products_visible(context):
    _catalogue().browse_catalogue()


@when("the shopper searches for the first product name")
def step_shopper_searches_for_first_product_name(context):
    cat = _catalogue()
    cards = cat.get_product_listing().cards
    assert cards, "product cards must be visible to search"
    term = cards[0].title.split(" ")[0]
    context.captured_search_term = term
    cat.search_for(term)


@when('the shopper searches for "{term}"')
def step_shopper_searches_for_term(context, term):
    context.captured_search_term = term
    _catalogue().search_for(term)


@then("the URL should contain the search term")
def step_url_should_contain_search_term(context):
    url = _catalogue().current_url()
    expected = f"/search/{context.captured_search_term}"
    assert expected in url, (
        f"URL should contain {expected!r}, got {url!r}"
    )


@then("search results should be displayed")
def step_search_results_should_be_displayed(context):
    cards = _catalogue().get_search_results().cards
    assert cards, "search results should show at least one product card"


@then("no results or empty state should be shown")
def step_no_results_or_empty_state(context):
    results = _catalogue().get_search_results()
    assert results.empty_state_visible or not results.cards, (
        "either no-results element or zero product cards expected"
    )
