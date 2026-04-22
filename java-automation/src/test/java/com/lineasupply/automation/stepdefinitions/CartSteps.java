package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.dsl.LineasupplyProtocol;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.java.Log;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@Log
public class CartSteps {

    private final LineasupplyProtocol protocol = DriverFactory.create();
    private int    notedItemCount;
    private String notedTotal;

    @Given("the shopper is on the homepage with products loaded")
    public void shopperOnHomepageWithProductsLoaded() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("home page loaded with products");
    }

    @Given("the shopper notes the current cart count")
    public void shopperNotesCurrentCartCount() {
        notedItemCount = protocol.getCartState().itemCount();
        log.fine("noted cart count: " + notedItemCount);
    }

    @When("the shopper adds the product to the cart")
    public void shopperAddsProductToCart() {
        protocol.addProductToCart();
        log.fine("added product to cart");
    }

    @When("the shopper navigates to the cart page")
    public void shopperNavigatesToCartPage() {
        protocol.openCartPage();
        log.fine("navigated to cart page");
    }

    @When("the shopper returns to the homepage")
    public void shopperReturnsToHomepage() {
        protocol.openHomePage();
        protocol.waitForProductsToLoad();
        log.fine("returned to home page");
    }

    @When("the shopper changes the quantity to {int}")
    public void shopperChangesQuantityTo(int quantity) {
        protocol.changeQuantityTo(quantity);
        log.fine("changed quantity to " + quantity);
    }

    @When("the shopper removes the first cart item")
    public void shopperRemovesFirstCartItem() {
        protocol.removeFirstItemFromCart();
        log.fine("removed first cart item");
    }

    @When("the shopper notes the current cart total")
    public void shopperNotesCurrentCartTotal() {
        notedTotal = protocol.getCartState().total();
        log.fine("noted cart total: " + notedTotal);
    }

    @Then("the cart badge should show {int} item(s)")
    public void cartBadgeShouldShow(int expectedCount) {
        protocol.waitForCartCountToBe(expectedCount);
        assertThat(protocol.getCartState().itemCount())
            .as("cart badge should show " + expectedCount + " item(s)")
            .isEqualTo(expectedCount);
        log.fine("cart badge shows " + expectedCount + " item(s)");
    }

    @Then("the cart badge should have increased by {int}")
    public void cartBadgeShouldHaveIncreasedBy(int increment) {
        int expected = notedItemCount + increment;
        protocol.waitForCartCountToBe(expected);
        assertThat(protocol.getCartState().itemCount())
            .as("cart badge should have increased by " + increment)
            .isEqualTo(expected);
        log.fine("cart badge increased by " + increment + " to " + expected);
    }

    @Then("the cart should contain at least {int} items")
    public void cartShouldContainAtLeastItems(int minimum) {
        protocol.waitForCartItemsToAppear();
        assertThat(protocol.getCartState().items().size())
            .as("cart should contain at least " + minimum + " items")
            .isGreaterThanOrEqualTo(minimum);
        log.fine("cart contains at least " + minimum + " items");
    }

    @Then("the cart total should be visible and show a price")
    public void cartTotalShouldBeVisibleAndShowPrice() {
        var total = protocol.getCartState().total();
        assertThat(total)
            .as("cart total should show a price with $")
            .matches(".*\\$\\d+.*");
        log.fine("cart total shows price: " + total);
    }

    @Then("the cart total should have changed")
    public void cartTotalShouldHaveChanged() {
        protocol.waitForCartTotalToChange(notedTotal);
        assertThat(protocol.getCartState().total())
            .as("cart total should have changed from " + notedTotal)
            .isNotEqualTo(notedTotal);
        log.fine("cart total changed from " + notedTotal);
    }

    @Then("the cart should show an empty state")
    public void cartShouldShowEmptyState() {
        protocol.waitForCartToBeEmpty();
        assertThat(protocol.getCartState().isEmpty())
            .as("cart should show empty state")
            .isTrue();
        log.fine("cart is empty");
    }

    @Then("the first cart item should be visible")
    public void firstCartItemShouldBeVisible() {
        protocol.waitForCartItemsToAppear();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(protocol.getCartState().items())
            .as("cart should have at least one item")
            .isNotEmpty();
        soft.assertAll();
        log.fine("first cart item is visible");
    }
}
