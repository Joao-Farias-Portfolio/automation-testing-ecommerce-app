package com.lineasupply.automation.dsl;

import org.assertj.core.api.SoftAssertions;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductDetailDsl {

    private static final Logger log = Logger.getLogger(ProductDetailDsl.class.getName());

    private final LineasupplyProtocol protocol;
    private String capturedListingTitle;
    private int    notedCartCount;

    public ProductDetailDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void openHomePageAndWaitForProducts() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("home page loaded with products");
    }

    public void viewFirstProductCapturingTitle() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        capturedListingTitle = protocol.getProductListing().cards().getFirst().title();
        protocol.clickFirstProductCard();
        log.fine("navigated to first product detail; listing title was: '" + capturedListingTitle + "'");
    }

    public void noteCurrentCartCount() {
        notedCartCount = protocol.getCartState().itemCount();
        log.fine("noted cart count: " + notedCartCount);
    }

    public void clickFirstProductCard() {
        protocol.clickFirstProductCard();
        log.fine("clicked first product card");
    }

    public void addProductToCartFromDetailPage() {
        protocol.addProductToCart();
        log.fine("added product to cart from detail page");
    }

    public void navigateBack() {
        protocol.navigateBack();
        protocol.waitForProductsToLoad();
        log.fine("navigated back to product listing");
    }

    public void assertUrlMatchesDetailPattern() {
        assertThat(protocol.currentUrl())
            .as("URL should match product detail pattern")
            .matches(".*/products/\\d+");
        log.fine("URL matches product detail pattern");
    }

    public void assertDetailTitleVisible() {
        assertThat(protocol.getProductDetail().title())
            .as("product title should be visible and non-blank on detail page")
            .isNotBlank();
        log.fine("product title is visible on detail page");
    }

    public void assertDetailShowsPriceDescriptionAndImage() {
        var detail = protocol.getProductDetail();
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

    public void assertDetailTitleMatchesListing() {
        assertThat(protocol.getProductDetail().title())
            .as("detail title should contain the listing title")
            .contains(capturedListingTitle.trim());
        log.fine("detail title matches listing title: '" + capturedListingTitle + "'");
    }

    public void assertAddToCartButtonShowsAdded() {
        var detail = protocol.getProductDetail();
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

    public void assertBackOnProductListing() {
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(protocol.getProductListing().cards())
            .as("product listing should be visible after navigating back")
            .isNotEmpty();
        soft.assertThat(protocol.currentUrl())
            .as("URL should be back on the listing")
            .matches(".*/(\\?.*)?$");
        soft.assertAll();
        log.fine("shopper is back on product listing");
    }
}
