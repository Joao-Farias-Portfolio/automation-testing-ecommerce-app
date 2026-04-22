package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.BrowsingDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class BrowseProductsSteps {

    private final BrowsingDsl browsing = new BrowsingDsl(DriverFactory.create());

    @Given("the shopper is on the homepage")
    public void shopperIsOnHomepage() {
        browsing.openHomePage();
    }

    @Given("the homepage has loaded with products")
    public void homepageHasLoadedWithProducts() {
        browsing.openHomePageAndWaitForProducts();
    }

    @Then("product cards should be visible")
    public void productCardsShouldBeVisible() {
        browsing.assertProductCardsVisible();
    }

    @Then("each product card should show a title and price")
    public void eachProductCardShouldShowTitleAndPrice() {
        browsing.assertEachCardHasTitleAndPrice();
    }

    @Then("the page should show a loading indicator briefly")
    public void pageShouldShowLoadingIndicatorBriefly() {
        browsing.assertLoadingCompletedSuccessfully();
    }

    @Then("product images should have valid sources")
    public void productImagesShouldHaveValidSources() {
        browsing.assertProductImagesValid();
    }
}
