package com.myecommerce.automation.dsl.steps;

import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class FilterSteps {

    private final CatalogueProtocol catalogue = DriverFactory.createCatalogue();
    private String capturedSearchTerm;

    @Given("the shopper is on the homepage with products visible")
    public void shopperOnHomepageWithProductsVisible() {
        catalogue.browseCatalogue();
        log.fine("home page loaded with products");
    }

    @When("the shopper searches for the first product name")
    public void shopperSearchesForFirstProductName() {
        var cards = catalogue.getProductListing().cards();
        assertThat(cards).as("product cards must be visible to search").isNotEmpty();
        capturedSearchTerm = cards.getFirst().title().split(" ")[0];
        catalogue.searchFor(capturedSearchTerm);
        log.fine("searched for first word of first product title: '" + capturedSearchTerm + "'");
    }

    @When("the shopper searches for {string}")
    public void shopperSearchesFor(String term) {
        capturedSearchTerm = term;
        catalogue.searchFor(term);
        log.fine("searched for: '" + term + "'");
    }

    @Then("the URL should contain the search term")
    public void urlShouldContainSearchTerm() {
        assertThat(catalogue.currentUrl())
            .as("URL should contain the searched term")
            .contains("/search/" + capturedSearchTerm);
        log.fine("URL contains search term: " + capturedSearchTerm);
    }

    @Then("search results should be displayed")
    public void searchResultsShouldBeDisplayed() {
        assertThat(catalogue.getSearchResults().cards())
            .as("search results should show at least one product card")
            .isNotEmpty();
        log.fine("search results are displayed");
    }

    @Then("no results or empty state should be shown")
    public void noResultsOrEmptyStateShouldBeShown() {
        var results = catalogue.getSearchResults();
        assertThat(results.emptyStateVisible() || results.cards().isEmpty())
            .as("either no-results element or zero product cards expected")
            .isTrue();
        log.fine("empty state or no results shown");
    }
}
