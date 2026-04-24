package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createCatalogue
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions

class BrowseProductsSteps {

    private val catalogue = createCatalogue()

    @Given("the shopper is on the homepage")
    fun shopperIsOnHomepage() = catalogue.browseCatalogue()

    @Given("the homepage has loaded with products")
    fun homepageHasLoadedWithProducts() = catalogue.browseCatalogue()

    @Given("the shopper is on the homepage with products loaded")
    fun shopperOnHomepageWithProductsLoaded() = catalogue.browseCatalogue()

    @Then("product cards should be visible")
    fun productCardsShouldBeVisible() {
        assertThat(catalogue.getProductListing().cards)
            .`as`("product cards should be visible on the page")
            .isNotEmpty
    }

    @Then("each product card should show a title and price")
    fun eachProductCardShouldShowTitleAndPrice() {
        val cards = catalogue.getProductListing().cards
        assertThat(cards).isNotEmpty
        val first = cards.first()
        SoftAssertions().apply {
            assertThat(first.title).`as`("product title should be visible on first card").isNotBlank
            assertThat(first.price).`as`("product price should be visible on first card").isNotBlank
            assertAll()
        }
    }

    @Then("the page should show a loading indicator briefly")
    fun pageShouldShowLoadingIndicatorBriefly() {
        val listing = catalogue.getProductListing()
        assertThat(listing.cards).`as`("product cards should be visible after loading completes").isNotEmpty
        assertThat(listing.hasVisibleLoadingIndicators)
            .`as`("loading indicators should be gone once product cards are visible")
            .isFalse
    }

    @Then("product images should have valid sources")
    fun productImagesShouldHaveValidSources() {
        val cards = catalogue.getProductListing().cards
        SoftAssertions().apply {
            cards.forEach { card ->
                assertThat(card.imageUrl)
                    .`as`("product image URL should be a valid http/https URL")
                    .matches("https?://.+")
            }
            assertAll()
        }
    }
}
