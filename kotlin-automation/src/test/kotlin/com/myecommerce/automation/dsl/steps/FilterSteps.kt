package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createCatalogue
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat

class FilterSteps {

    private val catalogue = createCatalogue()
    private var capturedSearchTerm: String = ""

    @Given("the shopper is on the homepage with products visible")
    fun shopperOnHomepageWithProductsVisible() = catalogue.browseCatalogue()

    @When("the shopper searches for the first product name")
    fun shopperSearchesForFirstProductName() {
        val cards = catalogue.getProductListing().cards
        assertThat(cards).`as`("product cards must be visible to search").isNotEmpty
        capturedSearchTerm = cards.first().title.split(" ")[0]
        catalogue.searchFor(capturedSearchTerm)
    }

    @When("the shopper searches for {string}")
    fun shopperSearchesFor(term: String) {
        capturedSearchTerm = term
        catalogue.searchFor(term)
    }

    @Then("the URL should contain the search term")
    fun urlShouldContainSearchTerm() {
        assertThat(catalogue.currentUrl())
            .`as`("URL should contain the searched term")
            .contains("/search/$capturedSearchTerm")
    }

    @Then("search results should be displayed")
    fun searchResultsShouldBeDisplayed() {
        assertThat(catalogue.getSearchResults().cards)
            .`as`("search results should show at least one product card")
            .isNotEmpty
    }

    @Then("no results or empty state should be shown")
    fun noResultsOrEmptyStateShouldBeShown() {
        val results = catalogue.getSearchResults()
        assertThat(results.emptyStateVisible || results.cards.isEmpty())
            .`as`("either no-results element or zero product cards expected")
            .isTrue
    }
}
