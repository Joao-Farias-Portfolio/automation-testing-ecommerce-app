package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.FilterDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class FilterSteps {

    private final FilterDsl filter = new FilterDsl(DriverFactory.create());

    @Given("the shopper is on the homepage with products visible")
    public void shopperOnHomepageWithProductsVisible() {
        filter.openHomePageAndWaitForProducts();
    }

    @When("the shopper searches for the first product name")
    public void shopperSearchesForFirstProductName() {
        filter.searchForFirstProductName();
    }

    @When("the shopper searches for {string}")
    public void shopperSearchesFor(String term) {
        filter.searchFor(term);
    }

    @Then("the URL should contain the search term")
    public void urlShouldContainSearchTerm() {
        filter.assertUrlContainsCapturedTerm();
    }

    @Then("search results should be displayed")
    public void searchResultsShouldBeDisplayed() {
        filter.assertSearchResultsDisplayed();
    }

    @Then("no results or empty state should be shown")
    public void noResultsOrEmptyStateShouldBeShown() {
        filter.assertEmptyOrNoResults();
    }
}
