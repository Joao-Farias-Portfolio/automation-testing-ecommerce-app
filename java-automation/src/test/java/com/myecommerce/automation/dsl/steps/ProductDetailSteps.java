package com.myecommerce.automation.dsl.steps;

import com.myecommerce.automation.dsl.protocols.CartProtocol;
import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;
import org.assertj.core.api.SoftAssertions;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class ProductDetailSteps {

    private final CatalogueProtocol catalogue = DriverFactory.createCatalogue();
    private final Supplier<CartProtocol> cart = DriverFactory::createCart;
    private String capturedListingTitle;
    private int    notedCartCount;

    @Given("the shopper is viewing the first product")
    public void shopperIsViewingFirstProduct() {
        catalogue.browseCatalogue();
        capturedListingTitle = catalogue.getProductListing().cards().getFirst().title();
        catalogue.viewFirstProduct();
        log.fine("navigated to first product detail; listing title was: '" + capturedListingTitle + "'");
    }

    @Given("the shopper notes the cart count on the detail page")
    public void shopperNotesCartCountOnDetailPage() {
        notedCartCount = cart.get().getCartState().itemCount();
        log.fine("noted cart count: " + notedCartCount);
    }

    @When("the shopper clicks the first product card")
    public void shopperClicksFirstProductCard() {
        catalogue.viewFirstProduct();
        log.fine("clicked first product card");
    }

    @When("the shopper adds the product to the cart from the detail page")
    public void shopperAddsProductFromDetailPage() {
        cart.get().addProductToCart();
        log.fine("added product to cart from detail page");
    }

    @When("the shopper navigates back")
    public void shopperNavigatesBack() {
        catalogue.returnToProductListing();
        log.fine("navigated back to product listing");
    }

    @Then("the URL should match the product detail pattern")
    public void urlShouldMatchProductDetailPattern() {
        assertThat(catalogue.currentUrl())
            .as("URL should match product detail pattern")
            .matches(".*/products/\\d+");
        log.fine("URL matches product detail pattern");
    }

    @Then("the product title should be visible on the detail page")
    public void productTitleShouldBeVisibleOnDetailPage() {
        assertThat(catalogue.getProductDetail().title())
            .as("product title should be visible and non-blank on detail page")
            .isNotBlank();
        log.fine("product title is visible on detail page");
    }

    @Then("the product detail page should show price, description and image")
    public void detailPageShouldShowPriceDescriptionAndImage() {
        var detail = catalogue.getProductDetail();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(detail.price())
            .as("price should show a $ value")
            .matches(".*\\$\\d+.*");
        soft.assertThat(detail.description())
            .as("description should not be blank")
            .isNotBlank();
        soft.assertThat(detail.imagePresent())
            .as("product image should be present")
            .isTrue();
        soft.assertAll();
        log.fine("detail page shows price, description, and image");
    }

    @Then("the product title should match the one from the listing")
    public void productTitleShouldMatchTheListing() {
        assertThat(catalogue.getProductDetail().title())
            .as("detail title should contain the listing title")
            .contains(capturedListingTitle.trim());
        log.fine("detail title matches listing title: '" + capturedListingTitle + "'");
    }

    @Then("the add to cart button should show Added to Cart and be disabled")
    public void addToCartButtonShouldShowAddedToCartAndBeDisabled() {
        var detail = catalogue.getProductDetail();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(detail.addToCartButtonText())
            .as("add-to-cart button text should say 'Added to Cart'")
            .containsIgnoringCase("Added to Cart");
        soft.assertThat(detail.addToCartEnabled())
            .as("add-to-cart button should be disabled after adding")
            .isFalse();
        soft.assertAll();
        log.fine("add-to-cart button shows 'Added to Cart' and is disabled");
    }

    @Then("the shopper should be back on the product listing")
    public void shopperShouldBeBackOnProductListing() {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(catalogue.getProductListing().cards())
            .as("product listing should be visible after navigating back")
            .isNotEmpty();
        soft.assertThat(catalogue.currentUrl())
            .as("URL should be back on the listing")
            .matches(".*/(\\?.*)?$");
        soft.assertAll();
        log.fine("shopper is back on product listing");
    }
}
