package com.myecommerce.automation.dsl.steps

import com.myecommerce.automation.dsl.protocols.createCart
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions

class CartSteps {

    private val cart by lazy { createCart() }

    private var notedItemCount: Int = 0
    private var notedTotal: String = ""

    @Given("the shopper notes the current cart count")
    fun shopperNotesCurrentCartCount() {
        notedItemCount = cart.getCartState().itemCount
    }

    @When("the shopper adds the product to the cart")
    fun shopperAddsProductToCart() = cart.addProductToCart()

    @When("the shopper navigates to the cart page")
    fun shopperNavigatesToCartPage() = cart.viewCart()

    @When("the shopper returns to the homepage")
    fun shopperReturnsToHomepage() = cart.browseCatalogue()

    @When("the shopper changes the quantity to {int}")
    fun shopperChangesQuantityTo(quantity: Int) = cart.changeQuantityTo(quantity)

    @When("the shopper removes the first cart item")
    fun shopperRemovesFirstCartItem() = cart.removeFirstItemFromCart()

    @When("the shopper notes the current cart total")
    fun shopperNotesCurrentCartTotal() {
        notedTotal = cart.getCartState().total
    }

    @Then("the cart badge should show {int} item(s)")
    fun cartBadgeShouldShow(expectedCount: Int) {
        assertThat(cart.getCartState().itemCount)
            .`as`("cart badge should show $expectedCount item(s)")
            .isEqualTo(expectedCount)
    }

    @Then("the cart badge should have increased by {int}")
    fun cartBadgeShouldHaveIncreasedBy(increment: Int) {
        val expected = notedItemCount + increment
        assertThat(cart.getCartState().itemCount)
            .`as`("cart badge should have increased by $increment")
            .isEqualTo(expected)
    }

    @Then("the cart should contain at least {int} items")
    fun cartShouldContainAtLeastItems(minimum: Int) {
        assertThat(cart.getCartState().items.size)
            .`as`("cart should contain at least $minimum items")
            .isGreaterThanOrEqualTo(minimum)
    }

    @Then("the cart total should be visible and show a price")
    fun cartTotalShouldBeVisibleAndShowPrice() {
        assertThat(cart.getCartState().total)
            .`as`("cart total should show a price with \$")
            .matches(".*\\$\\d+.*")
    }

    @Then("the cart total should have changed")
    fun cartTotalShouldHaveChanged() {
        assertThat(cart.getCartState().total)
            .`as`("cart total should have changed from $notedTotal")
            .isNotEqualTo(notedTotal)
    }

    @Then("the cart should show an empty state")
    fun cartShouldShowEmptyState() {
        assertThat(cart.getCartState().isEmpty)
            .`as`("cart should show empty state")
            .isTrue
    }

    @Then("the first cart item should be visible")
    fun firstCartItemShouldBeVisible() {
        SoftAssertions().apply {
            assertThat(cart.getCartState().items).`as`("cart should have at least one item").isNotEmpty
            assertAll()
        }
    }
}
