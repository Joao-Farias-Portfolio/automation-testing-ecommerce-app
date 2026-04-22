package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.dsl.LineasupplyProtocol;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class FilterSteps {

    private final LineasupplyProtocol protocol = DriverFactory.create();
    private String capturedSearchTerm;

    @Given("the shopper is on the homepage with products visible")
    public void shopperOnHomepageWithProductsVisible() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("home page loaded with products");
    }

    @When("the shopper searches for the first product name")
    public void shopperSearchesForFirstProductName() {
        var cards = protocol.getProductListing().cards();
        assertThat(cards).as("product cards must be visible to search").isNotEmpty();
        capturedSearchTerm = cards.getFirst().title().split(" ")[0];
        protocol.searchFor(capturedSearchTerm);
        log.fine("searched for first word of first product title: '" + capturedSearchTerm + "'");
    }

    @When("the shopper searches for {string}")
    public void shopperSearchesFor(String term) {
        capturedSearchTerm = term;
        protocol.searchFor(term);
        log.fine("searched for: '" + term + "'");
    }

    @Then("the URL should contain the search term")
    public void urlShouldContainSearchTerm() {
        protocol.waitForSearchResultsToLoad();
        assertThat(protocol.currentUrl())
            .as("URL should contain the searched term")
            .contains("/search/" + capturedSearchTerm);
        log.fine("URL contains search term: " + capturedSearchTerm);
    }

    @Then("search results should be displayed")
    public void searchResultsShouldBeDisplayed() {
        protocol.waitForSearchResultsToLoad();
        assertThat(protocol.getSearchResults().cards())
            .as("search results should show at least one product card")
            .isNotEmpty();
        log.fine("search results are displayed");
    }

    @Then("no results or empty state should be shown")
    public void noResultsOrEmptyStateShouldBeShown() {
        protocol.waitForSearchResultsToLoad();
        var results = protocol.getSearchResults();
        assertThat(results.emptyStateVisible() || results.cards().isEmpty())
            .as("either no-results element or zero product cards expected")
            .isTrue();
        log.fine("empty state or no results shown");
    }
}
