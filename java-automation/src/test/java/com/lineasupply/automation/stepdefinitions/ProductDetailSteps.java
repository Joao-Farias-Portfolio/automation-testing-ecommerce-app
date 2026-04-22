package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.ProductDetailDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProductDetailSteps {

    private final ProductDetailDsl detail = new ProductDetailDsl(DriverFactory.create());

    @Given("the shopper is viewing the first product")
    public void shopperIsViewingFirstProduct() {
        detail.viewFirstProductCapturingTitle();
    }

    @Given("the shopper notes the cart count on the detail page")
    public void shopperNotesCartCountOnDetailPage() {
        detail.noteCurrentCartCount();
    }

    @When("the shopper clicks the first product card")
    public void shopperClicksFirstProductCard() {
        detail.clickFirstProductCard();
    }

    @When("the shopper adds the product to the cart from the detail page")
    public void shopperAddsProductFromDetailPage() {
        detail.addProductToCartFromDetailPage();
    }

    @When("the shopper navigates back")
    public void shopperNavigatesBack() {
        detail.navigateBack();
    }

    @Then("the URL should match the product detail pattern")
    public void urlShouldMatchProductDetailPattern() {
        detail.assertUrlMatchesDetailPattern();
    }

    @Then("the product title should be visible on the detail page")
    public void productTitleShouldBeVisibleOnDetailPage() {
        detail.assertDetailTitleVisible();
    }

    @Then("the product detail page should show price, description and image")
    public void detailPageShouldShowPriceDescriptionAndImage() {
        detail.assertDetailShowsPriceDescriptionAndImage();
    }

    @Then("the product title should match the one from the listing")
    public void productTitleShouldMatchTheListing() {
        detail.assertDetailTitleMatchesListing();
    }

    @Then("the add to cart button should show Added to Cart and be disabled")
    public void addToCartButtonShouldShowAddedToCartAndBeDisabled() {
        detail.assertAddToCartButtonShowsAdded();
    }

    @Then("the shopper should be back on the product listing")
    public void shopperShouldBeBackOnProductListing() {
        detail.assertBackOnProductListing();
    }
}
