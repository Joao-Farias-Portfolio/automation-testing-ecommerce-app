package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createCart
import com.myecommerce.automation.dsl.protocols.createCatalogue
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions

class ProductDetailSteps {

    private val catalogue = createCatalogue()
    private val cart by lazy { createCart() }

    private var capturedListingTitle: String = ""
    private var notedCartCount: Int = 0

    @Given("the shopper is viewing the first product")
    fun shopperIsViewingFirstProduct() {
        catalogue.browseCatalogue()
        capturedListingTitle = catalogue.getProductListing().cards.first().title
        catalogue.viewFirstProduct()
    }

    @Given("the shopper notes the cart count on the detail page")
    fun shopperNotesCartCountOnDetailPage() {
        notedCartCount = cart.getCartState().itemCount
    }

    @When("the shopper clicks the first product card")
    fun shopperClicksFirstProductCard() = catalogue.viewFirstProduct()

    @When("the shopper adds the product to the cart from the detail page")
    fun shopperAddsProductFromDetailPage() = cart.addProductToCart()

    @When("the shopper navigates back")
    fun shopperNavigatesBack() = catalogue.returnToProductListing()

    @Then("the URL should match the product detail pattern")
    fun urlShouldMatchProductDetailPattern() {
        assertThat(catalogue.currentUrl())
            .`as`("URL should match product detail pattern")
            .matches(".*/products/\\d+")
    }

    @Then("the product title should be visible on the detail page")
    fun productTitleShouldBeVisibleOnDetailPage() {
        assertThat(catalogue.getProductDetail().title)
            .`as`("product title should be visible and non-blank on detail page")
            .isNotBlank
    }

    @Then("the product detail page should show price, description and image")
    fun detailPageShouldShowPriceDescriptionAndImage() {
        val detail = catalogue.getProductDetail()
        SoftAssertions().apply {
            assertThat(detail.price).`as`("price should show a \$ value").matches(".*\\$\\d+.*")
            assertThat(detail.description).`as`("description should not be blank").isNotBlank
            assertThat(detail.imagePresent).`as`("product image should be present").isTrue
            assertAll()
        }
    }

    @Then("the product title should match the one from the listing")
    fun productTitleShouldMatchTheListing() {
        assertThat(catalogue.getProductDetail().title)
            .`as`("detail title should contain the listing title")
            .contains(capturedListingTitle.trim())
    }

    @Then("the add to cart button should show Added to Cart and be disabled")
    fun addToCartButtonShouldShowAddedToCartAndBeDisabled() {
        val detail = catalogue.getProductDetail()
        SoftAssertions().apply {
            assertThat(detail.addToCartButtonText)
                .`as`("add-to-cart button text should say 'Added to Cart'")
                .containsIgnoringCase("Added to Cart")
            assertThat(detail.addToCartEnabled)
                .`as`("add-to-cart button should be disabled after adding")
                .isFalse
            assertAll()
        }
    }

    @Then("the shopper should be back on the product listing")
    fun shopperShouldBeBackOnProductListing() {
        SoftAssertions().apply {
            assertThat(catalogue.getProductListing().cards)
                .`as`("product listing should be visible after navigating back")
                .isNotEmpty
            assertThat(catalogue.currentUrl())
                .`as`("URL should be back on the listing")
                .matches(".*/(\\?.*)?$")
            assertAll()
        }
    }
}
