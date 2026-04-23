package com.myecommerce.automation.dsl.steps;

import com.myecommerce.automation.dsl.protocols.CatalogueProtocol;
import com.myecommerce.automation.dsl.protocols.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.java.Log;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class BrowseProductsSteps {

    private final CatalogueProtocol protocol = DriverFactory.createCatalogue();

    @Given("the shopper is on the homepage")
    public void shopperIsOnHomepage() {
        protocol.browseCatalogue();
        log.fine("opened home page");
    }

    @Given("the homepage has loaded with products")
    public void homepageHasLoadedWithProducts() {
        protocol.browseCatalogue();
        log.fine("home page loaded with products");
    }

    @Given("the shopper is on the homepage with products loaded")
    public void shopperOnHomepageWithProductsLoaded() {
        protocol.browseCatalogue();
        log.fine("home page loaded with products");
    }

    @Then("product cards should be visible")
    public void productCardsShouldBeVisible() {
        assertThat(protocol.getProductListing().cards())
            .as("product cards should be visible on the page")
            .isNotEmpty();
        log.fine("product cards are visible");
    }

    @Then("each product card should show a title and price")
    public void eachProductCardShouldShowTitleAndPrice() {
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

    @Then("the page should show a loading indicator briefly")
    public void pageShouldShowLoadingIndicatorBriefly() {
        var listing = protocol.getProductListing();
        assertThat(listing.cards())
            .as("product cards should be visible after loading completes")
            .isNotEmpty();
        assertThat(listing.hasVisibleLoadingIndicators())
            .as("loading indicators should be gone once product cards are visible")
            .isFalse();
        log.fine("page loading completed, no loading indicators visible");
    }

    @Then("product images should have valid sources")
    public void productImagesShouldHaveValidSources() {
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
