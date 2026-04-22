package com.lineasupply.automation.dsl;

import org.assertj.core.api.SoftAssertions;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class BrowsingDsl {

    private static final Logger log = Logger.getLogger(BrowsingDsl.class.getName());

    private final LineasupplyProtocol protocol;

    public BrowsingDsl(LineasupplyProtocol protocol) {
        this.protocol = protocol;
    }

    public void openHomePage() {
        protocol.openHomePage();
        log.fine("opened home page");
    }

    public void openHomePageAndWaitForProducts() {
        protocol.openHomePage();
        protocol.getProductListing();
        log.fine("home page loaded with products");
    }

    public void assertProductCardsVisible() {
        assertThat(protocol.getProductListing().cards())
            .as("product cards should be visible on the page")
            .isNotEmpty();
        log.fine("product cards are visible");
    }

    public void assertEachCardHasTitleAndPrice() {
        var cards = protocol.getProductListing().cards();
        assertThat(cards).isNotEmpty();
        var firstCard = cards.getFirst();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(firstCard.title())
            .as("product title should be visible on first card")
            .isNotBlank();
        soft.assertThat(firstCard.price())
            .as("product price should be visible on first card")
            .isNotBlank();
        soft.assertAll();
        log.fine("first product card has title and price");
    }

    public void assertLoadingCompletedSuccessfully() {
        var listing = protocol.getProductListing();
        assertThat(listing.cards())
            .as("product cards should be visible after loading completes")
            .isNotEmpty();
        assertThat(listing.hasVisibleLoadingIndicators())
            .as("loading indicators should be gone once product cards are visible")
            .isFalse();
        log.fine("page loading completed, no loading indicators visible");
    }

    public void assertProductImagesValid() {
        var cards = protocol.getProductListing().cards();
        SoftAssertions soft = new SoftAssertions();
        cards.forEach(card ->
            soft.assertThat(card.imageUrl())
                .as("product image URL should be a valid http/https URL")
                .matches("https?://.+"));
        soft.assertAll();
        log.fine("product images have valid URLs");
    }
}
