package com.lineasupply.automation.dsl;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class FilterDsl {

    private static final Logger log = Logger.getLogger(FilterDsl.class.getName());

    private final LineasupplyProtocol protocol;
    private String capturedSearchTerm;

    public FilterDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void openHomePageAndWaitForProducts() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("home page loaded with products");
    }

    public void searchForFirstProductName() {
        var cards = protocol.getProductListing().cards();
        assertThat(cards).as("product cards must be visible to search").isNotEmpty();
        capturedSearchTerm = cards.getFirst().title().split(" ")[0];
        protocol.searchFor(capturedSearchTerm);
        log.fine("searched for first word of first product title: '" + capturedSearchTerm + "'");
    }

    public void searchFor(String term) {
        capturedSearchTerm = term;
        protocol.searchFor(term);
        log.fine("searched for: '" + term + "'");
    }

    public void assertUrlContainsCapturedTerm() {
        protocol.waitForSearchResultsToLoad();
        assertThat(protocol.currentUrl())
            .as("URL should contain the searched term")
            .contains("/search/" + capturedSearchTerm);
        log.fine("URL contains search term: " + capturedSearchTerm);
    }

    public void assertSearchResultsDisplayed() {
        protocol.waitForSearchResultsToLoad();
        assertThat(protocol.getSearchResults().cards())
            .as("search results should show at least one product card")
            .isNotEmpty();
        log.fine("search results are displayed");
    }

    public void assertEmptyOrNoResults() {
        protocol.waitForSearchResultsToLoad();
        var results = protocol.getSearchResults();
        assertThat(results.emptyStateVisible() || results.cards().isEmpty())
            .as("either no-results element or zero product cards expected")
            .isTrue();
        log.fine("empty state or no results shown");
    }
}
