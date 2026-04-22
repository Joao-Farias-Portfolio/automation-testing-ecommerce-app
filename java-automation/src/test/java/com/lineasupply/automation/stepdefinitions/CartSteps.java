package com.lineasupply.automation.stepdefinitions;

import com.lineasupply.automation.driver.DriverFactory;
import com.lineasupply.automation.dsl.CartDsl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CartSteps {

    private final CartDsl cart = new CartDsl(DriverFactory.create());

    @Given("the shopper is on the homepage with products loaded")
    public void shopperOnHomepageWithProductsLoaded() {
        cart.openHomePageAndWaitForProducts();
    }

    @Given("the shopper notes the current cart count")
    public void shopperNotesCurrentCartCount() {
        cart.noteCurrentCartCount();
    }

    @When("the shopper adds the product to the cart")
    public void shopperAddsProductToCart() {
        cart.addProduct();
    }

    @When("the shopper navigates to the cart page")
    public void shopperNavigatesToCartPage() {
        cart.navigateToCart();
    }

    @When("the shopper returns to the homepage")
    public void shopperReturnsToHomepage() {
        cart.returnToHomePage();
    }

    @When("the shopper changes the quantity to {int}")
    public void shopperChangesQuantityTo(int quantity) {
        cart.changeQuantityTo(quantity);
    }

    @When("the shopper removes the first cart item")
    public void shopperRemovesFirstCartItem() {
        cart.removeFirstItem();
    }

    @When("the shopper notes the current cart total")
    public void shopperNotesCurrentCartTotal() {
        cart.noteCurrentCartTotal();
    }

    @Then("the cart badge should show {int} item(s)")
    public void cartBadgeShouldShow(int expectedCount) {
        cart.assertBadgeShowsCount(expectedCount);
    }

    @Then("the cart badge should have increased by {int}")
    public void cartBadgeShouldHaveIncreasedBy(int increment) {
        cart.assertBadgeIncreasedBy(increment);
    }

    @Then("the cart should contain at least {int} items")
    public void cartShouldContainAtLeastItems(int minimum) {
        cart.assertCartContainsAtLeastItems(minimum);
    }

    @Then("the cart total should be visible and show a price")
    public void cartTotalShouldBeVisibleAndShowPrice() {
        cart.assertTotalShowsPrice();
    }

    @Then("the cart total should have changed")
    public void cartTotalShouldHaveChanged() {
        cart.assertTotalChanged();
    }

    @Then("the cart should show an empty state")
    public void cartShouldShowEmptyState() {
        cart.assertEmptyState();
    }

    @Then("the first cart item should be visible")
    public void firstCartItemShouldBeVisible() {
        cart.assertFirstItemVisible();
    }
}
